package com.butent.bee.egg.client;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.layout.BeeStack;
import com.butent.bee.egg.client.layout.BeeTab;
import com.butent.bee.egg.client.menu.BeeMenuBar;
import com.butent.bee.egg.client.menu.BeeMenuItem;
import com.butent.bee.egg.client.menu.BeeMenuItemSeparator;
import com.butent.bee.egg.client.menu.MenuCommand;
import com.butent.bee.egg.client.menu.MenuDataProvider;
import com.butent.bee.egg.client.menu.MenuSelectionHandler;
import com.butent.bee.egg.client.menu.MenuTreeViewModel;
import com.butent.bee.egg.client.tree.BeeCellBrowser;
import com.butent.bee.egg.client.tree.BeeCellTree;
import com.butent.bee.egg.client.tree.BeeTree;
import com.butent.bee.egg.client.tree.BeeTreeItem;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.client.widget.BeeCellList;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.menu.MenuConst;
import com.butent.bee.egg.shared.menu.MenuEntry;
import com.butent.bee.egg.shared.menu.MenuUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class BeeMenu implements BeeModule {
  private List<MenuEntry> roots = null;
  private List<MenuEntry> items = null;

  private List<String> layouts = new ArrayList<String>();
  private boolean[] options = null;
  private int[] limits = null;

  public BeeMenu() {
    super();
  }

  public boolean drawMenu() {
    Assert.state(validState());

    layouts.clear();
    options = new boolean[MenuConst.MAX_MENU_DEPTH];

    limits = new int[MenuConst.MAX_MENU_DEPTH];
    int rLim = BeeGlobal.getFieldInt(MenuConst.FIELD_ROOT_LIMIT);
    int iLim = BeeGlobal.getFieldInt(MenuConst.FIELD_ITEM_LIMIT);

    for (int i = MenuConst.ROOT_MENU_INDEX; i < MenuConst.MAX_MENU_DEPTH; i++) {
      layouts.add(BeeGlobal.getFieldValue(MenuConst.fieldMenuLayout(i)));
      options[i] = BeeGlobal.getFieldBoolean(MenuConst.fieldMenuBarType(i));
      limits[i] = MenuConst.isRootLevel(i) ? rLim : iLim;
    }

    boolean debug = BeeGlobal.isDebug();
    BeeDuration dur = null;
    if (debug) {
      dur = new BeeDuration("draw menu");
    }

    Widget w = createMenu(0, MenuUtils.limitEntries(getRoots(), rLim), null);

    if (debug) {
      BeeKeeper.getLog().finish(dur);
    }

    boolean ok = (w != null);
    if (ok) {
      BeeKeeper.getUi().updateMenu(w);
    } else {
      BeeKeeper.getLog().severe("error creating menu");
    }

    return ok;
  }

  public void end() {
  }

  public List<MenuEntry> getItems() {
    return items;
  }

  public List<String> getLayouts() {
    return layouts;
  }

  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return 20;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public String getRootLayout() {
    return getLayout(0);
  }

  public List<MenuEntry> getRoots() {
    return roots;
  }

  public void init() {
  }

  public void loadCallBack(JsArrayString arr) {
    Assert.notNull(arr);
    int n = arr.length();
    Assert.isPositive(n);

    BeeDuration dur = new BeeDuration("load menu");

    clear();
    MenuEntry entry;

    int rc = 0;
    int ic = 0;

    for (int i = 0; i < n; i++) {
      entry = new MenuEntry();
      entry.deserialize(arr.get(i));

      if (!entry.isValid()) {
        BeeKeeper.getLog().severe("invalid menu entry", BeeUtils.bracket(i),
            arr.get(i));
        break;
      }

      if (entry.isRoot()) {
        roots.add(entry);
        rc++;
      } else {
        items.add(entry);
        ic++;
      }
    }

    BeeKeeper.getLog().finish(dur, BeeUtils.addName("roots", rc),
        BeeUtils.addName("items", ic));
  }

  public void loadMenu() {
    BeeKeeper.getRpc().dispatchService(BeeService.SERVICE_GET_MENU);
  }

  public void setItems(List<MenuEntry> items) {
    this.items = items;
  }

  public void setLayouts(List<String> layouts) {
    this.layouts = layouts;
  }

  public void setRoots(List<MenuEntry> roots) {
    this.roots = roots;
  }

  public void showMenu() {
    if (BeeUtils.allEmpty(getRoots(), getItems())) {
      BeeGlobal.showDialog("menu empty");
      return;
    }

    String[] cols = new String[]{
        "id", "parent", "order", "sep", "text", "service", "parameters",
        "type", "style", "key", "visible"};

    int rc = getRootCount();
    int ic = getItemCount();

    String[][] arr = new String[rc + ic][cols.length];
    MenuEntry entry;
    int j;

    for (int i = 0; i < rc + ic; i++) {
      if (i < rc) {
        entry = getRoots().get(i);
      } else {
        entry = getItems().get(i - rc);
      }

      j = 0;

      arr[i][j++] = entry.getId();
      arr[i][j++] = entry.getParent();
      arr[i][j++] = BeeUtils.transform(entry.getOrder());
      arr[i][j++] = BeeUtils.transform(entry.getSeparators());
      arr[i][j++] = entry.getText();
      arr[i][j++] = entry.getService();
      arr[i][j++] = entry.getParameters();
      arr[i][j++] = entry.getType();
      arr[i][j++] = entry.getStyle();
      arr[i][j++] = entry.getKeyName();
      arr[i][j++] = BeeUtils.toString(entry.isVisible());
    }

    BeeKeeper.getUi().showGrid(arr, cols);
  }

  public void start() {
    clear();
    loadMenu();
  }

  public boolean validState() {
    return BeeUtils.allNotEmpty(getRoots(), getItems());
  }

  private void addEntry(Widget rw, int itemCnt, MenuEntry item, Widget cw) {
    String txt = item.getText();
    String svc = item.getService();
    String opt = item.getParameters();

    boolean sepBefore = MenuConst.isSeparatorBefore(item.getSeparators());
    boolean sepAfter = MenuConst.isSeparatorAfter(item.getSeparators());

    if (rw instanceof BeeMenuBar) {
      BeeMenuBar mb = (BeeMenuBar) rw;
      if (sepBefore) {
        mb.addSeparator(new BeeMenuItemSeparator());
      }

      if (cw == null) {
        mb.addItem(txt, new MenuCommand(svc, opt));
      } else if (cw instanceof BeeMenuBar) {
        mb.addItem(txt, (BeeMenuBar) cw);
      }

      if (sepAfter) {
        mb.addSeparator(new BeeMenuItemSeparator());
      }

    } else if (rw instanceof BeeStack) {
      if (cw != null) {
        double header = BeeUtils.iif(itemCnt <= 10, 2.0, itemCnt >= 18, 1.2,
            (30.0 - itemCnt) / 10.0);
        ((BeeStack) rw).add(cw, txt, header);
      }
    } else if (rw instanceof BeeTab) {
      if (cw != null) {
        ((BeeTab) rw).add(cw, txt);
      }

    } else if (rw instanceof BeeTree) {
      BeeTreeItem it = new BeeTreeItem(txt);

      if (cw == null) {
        it.setUserObject(new MenuCommand(svc, opt));
      } else {
        it.addItem(cw);
      }

      ((BeeTree) rw).addItem(it);
    }
  }

  private void clear() {
    roots = new ArrayList<MenuEntry>();
    items = new ArrayList<MenuEntry>();
  }

  private Widget createMenu(int level, List<MenuEntry> entries, Widget parent) {
    Assert.betweenExclusive(level, MenuConst.ROOT_MENU_INDEX,
        MenuConst.MAX_MENU_DEPTH);
    Assert.notEmpty(entries);

    String layout = getLayout(level);
    boolean opt = getOption(level);

    if (parent instanceof BeeMenuBar) {
      int lvl = level - 1;
      while (lvl >= MenuConst.ROOT_MENU_INDEX && BeeUtils.same(layout, MenuConst.LAYOUT_TREE)) {
        layout = getLayout(lvl--);
      }
    }

    Widget rw = createWidget(layout, opt, entries, level);

    boolean lastLevel = (level >= MenuConst.MAX_MENU_DEPTH - 1);
    int limit = lastLevel ? getLimit(level) : getLimit(level + 1);

    List<MenuEntry> children = null;
    Widget cw = null;

    int cnt = entries.size();

    for (MenuEntry entry : entries) {
      if (!lastLevel) {
        children = MenuUtils.getChildren(getItems(), entry.getId(), true, limit);
      }

      if (BeeUtils.isEmpty(children)) {
        cw = null;
      } else {
        cw = createMenu(level + 1, children, rw);
      }

      addEntry(rw, cnt, entry, cw);
    }

    prepareWidget(rw, entries);

    return rw;
  }

  private Widget createWidget(String layout, boolean opt,
      List<MenuEntry> entries, int level) {
    Widget w = null;

    if (BeeUtils.same(layout, MenuConst.LAYOUT_MENU_HOR)) {
      w = new BeeMenuBar(level, false, getBarType(opt),
          BeeMenuItem.ITEM_TYPE.LABEL, opt);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_MENU_VERT)) {
      w = new BeeMenuBar(level, true, getBarType(opt),
          BeeMenuItem.ITEM_TYPE.LABEL, opt);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_STACK)) {
      w = new BeeStack(Unit.EM);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TAB)) {
      w = new BeeTab(20, Unit.PX);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TREE)) {
      w = new BeeTree(new MenuSelectionHandler());

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_CELL_TREE)) {
      w = new BeeCellTree(new MenuTreeViewModel(new MenuDataProvider(entries),
          new MenuDataProvider(getItems(), getItemLimit())), null);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_CELL_BROWSER)) {
      w = new BeeCellBrowser(new MenuTreeViewModel(
          new MenuDataProvider(entries), new MenuDataProvider(getItems(),
              getItemLimit())), null);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_LIST)) {
      w = new BeeMenuBar(level, true, BeeMenuBar.BAR_TYPE.LIST,
          BeeMenuItem.ITEM_TYPE.OPTION, opt);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_ORDERED_LIST)) {
      w = new BeeMenuBar(level, true, BeeMenuBar.BAR_TYPE.OLIST,
          BeeMenuItem.ITEM_TYPE.LI, opt);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_UNORDERED_LIST)) {
      w = new BeeMenuBar(level, true, BeeMenuBar.BAR_TYPE.ULIST,
          BeeMenuItem.ITEM_TYPE.LI, opt);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_DEFINITION_LIST)) {
      w = new BeeMenuBar(level, true, BeeMenuBar.BAR_TYPE.DLIST,
          BeeMenuItem.ITEM_TYPE.DT, opt);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_RADIO_HOR)) {
      w = new BeeMenuBar(level, false, getBarType(opt),
          BeeMenuItem.ITEM_TYPE.RADIO, opt);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_RADIO_VERT)) {
      w = new BeeMenuBar(level, true, getBarType(opt),
          BeeMenuItem.ITEM_TYPE.RADIO, opt);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_BUTTONS_HOR)) {
      w = new BeeMenuBar(level, false, getBarType(opt),
          BeeMenuItem.ITEM_TYPE.BUTTON, opt);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_BUTTONS_VERT)) {
      w = new BeeMenuBar(level, true, getBarType(opt),
          BeeMenuItem.ITEM_TYPE.BUTTON, opt);

    } else {
      Assert.untouchable();
    }

    return w;
  }

  private BeeMenuBar.BAR_TYPE getBarType(boolean opt) {
    return opt ? BeeMenuBar.BAR_TYPE.TABLE : BeeMenuBar.BAR_TYPE.FLOW;
  }

  private int getItemCount() {
    return (getItems() == null) ? 0 : getItems().size();
  }

  private int getItemLimit() {
    return getLimit(MenuConst.ROOT_MENU_INDEX + 1);
  }

  private String getLayout(int idx) {
    Assert.isIndex(getLayouts(), idx);
    return getLayouts().get(idx);
  }

  private int getLimit(int idx) {
    Assert.isIndex(limits, idx);
    return limits[idx];
  }

  private boolean getOption(int idx) {
    Assert.isIndex(options, idx);
    return options[idx];
  }

  private int getRootCount() {
    return (getRoots() == null) ? 0 : getRoots().size();
  }

  @SuppressWarnings("unchecked")
  private void prepareWidget(Widget w, List<MenuEntry> entries) {
    if (w instanceof BeeMenuBar) {
      ((BeeMenuBar) w).prepare();
    } else if (w instanceof BeeCellList) {
      int cnt = BeeUtils.length(entries);
      if (cnt > 0) {
        ((BeeCellList<MenuEntry>) w).setRowData(0, entries);
        ((BeeCellList<MenuEntry>) w).setRowCount(cnt);
      }
    }
  }

}
