package com.butent.bee.egg.client;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.layout.BeeStack;
import com.butent.bee.egg.client.layout.BeeTab;
import com.butent.bee.egg.client.menu.BeeMenuBar;
import com.butent.bee.egg.client.menu.BeeMenuItemSeparator;
import com.butent.bee.egg.client.menu.MenuCell;
import com.butent.bee.egg.client.menu.MenuCommand;
import com.butent.bee.egg.client.menu.MenuDataProvider;
import com.butent.bee.egg.client.menu.MenuTreeViewModel;
import com.butent.bee.egg.client.tree.BeeCellBrowser;
import com.butent.bee.egg.client.tree.BeeCellTree;
import com.butent.bee.egg.client.tree.BeeTree;
import com.butent.bee.egg.client.tree.BeeTreeItem;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.client.widget.BeeCellList;
import com.butent.bee.egg.client.widget.BeeDefinitionList;
import com.butent.bee.egg.client.widget.BeeHtmlList;
import com.butent.bee.egg.client.widget.BeeListBox;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeWidget;
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
  private List<BeeMenuBar.BAR_TYPE> barTypes = new ArrayList<BeeMenuBar.BAR_TYPE>();

  private int[] limits = null;

  public BeeMenu() {
    super();
  }

  public boolean drawMenu() {
    Assert.state(validState());

    layouts.clear();
    barTypes.clear();

    limits = new int[MenuConst.MAX_MENU_DEPTH];
    int rLim = BeeGlobal.getFieldInt(MenuConst.FIELD_ROOT_LIMIT);
    int iLim = BeeGlobal.getFieldInt(MenuConst.FIELD_ITEM_LIMIT);

    for (int i = MenuConst.ROOT_MENU_INDEX; i < MenuConst.MAX_MENU_DEPTH; i++) {
      layouts.add(BeeGlobal.getFieldValue(MenuConst.fieldMenuLayout(i)));
      barTypes.add(BeeGlobal.getFieldBoolean(MenuConst.fieldMenuBarType(i))
          ? BeeMenuBar.BAR_TYPE.TABLE : BeeMenuBar.BAR_TYPE.FLOW);

      limits[i] = MenuConst.isRootLevel(i) ? rLim : iLim;
    }

    boolean debug = BeeGlobal.isDebug();
    BeeDuration dur = null;
    if (debug) {
      dur = new BeeDuration("draw menu");
    }

    Widget w = createMenu(0, MenuUtils.limitEntries(getRoots(), rLim));

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

  public List<BeeMenuBar.BAR_TYPE> getBarTypes() {
    return barTypes;
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

  public void setBarTypes(List<BeeMenuBar.BAR_TYPE> barTypes) {
    this.barTypes = barTypes;
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

    BeeKeeper.getUi().updateActivePanel(BeeGlobal.createSimpleGrid(cols, arr));
  }

  public void start() {
    clear();
    loadMenu();
  }

  public boolean validState() {
    return BeeUtils.allNotEmpty(getRoots(), getItems());
  }

  private void addEntry(Widget rw, MenuEntry item, Widget cw) {
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
        ((BeeStack) rw).add(cw, txt, 2);
      }
    } else if (rw instanceof BeeTab) {
      if (cw != null) {
        ((BeeTab) rw).add(cw, txt);
      }  

    } else if (rw instanceof BeeTree) {
      BeeTreeItem it = new BeeTreeItem(txt);
      if (cw != null) {
        it.addItem(cw);
      }
      ((BeeTree) rw).addItem(it);

    } else if (rw instanceof BeeListBox) {
    ((BeeListBox) rw).addItem(txt);
    } else if (rw instanceof BeeHtmlList) {
      ((BeeHtmlList) rw).addItem(txt);
    } else if (rw instanceof BeeDefinitionList) {
      ((BeeDefinitionList) rw).addItem(txt);
    }
  }

  private void clear() {
    roots = new ArrayList<MenuEntry>();
    items = new ArrayList<MenuEntry>();
  }

  private Widget createMenu(int level, List<MenuEntry> entries) {
    Assert.betweenExclusive(level, MenuConst.ROOT_MENU_INDEX,
        MenuConst.MAX_MENU_DEPTH);
    Assert.notEmpty(entries);

    String layout = getLayout(level);
    BeeMenuBar.BAR_TYPE rbt = getBarType(level);

    Widget rw = createWidget(layout, rbt, entries);

    if (MenuConst.isRootLevel(level)
        && BeeUtils.inListSame(layout, MenuConst.LAYOUT_CELL_TREE,
            MenuConst.LAYOUT_CELL_BROWSER)) {
      return rw;
    }

    boolean lastLevel = (level >= MenuConst.MAX_MENU_DEPTH - 1);
    int limit = lastLevel ? getLimit(level) : getLimit(level + 1);

    List<MenuEntry> children = null;
    Widget cw = null;

    for (MenuEntry entry : entries) {
      if (!lastLevel) {
        children = MenuUtils.getChildren(getItems(), entry.getId(), true, limit);
      }

      if (BeeUtils.isEmpty(children)) {
        cw = null;
      } else {
        cw = createMenu(level + 1, children);
      }

      addEntry(rw, entry, cw);
    }

    prepareWidget(rw, entries);

    return rw;
  }

  private Widget createWidget(String layout, BeeMenuBar.BAR_TYPE barType,
      List<MenuEntry> entries) {
    Widget w = null;

    if (BeeUtils.same(layout, MenuConst.LAYOUT_MENU_HOR)) {
      w = new BeeMenuBar(false, barType);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_MENU_VERT)) {
      w = new BeeMenuBar(true, barType);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_STACK)) {
      w = new BeeStack(Unit.EM);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TAB)) {
      w = new BeeTab(20, Unit.PX);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TREE)) {
      w = new BeeTree();

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_CELL_TREE)) {
      w = new BeeCellTree(new MenuTreeViewModel(new MenuDataProvider(entries),
          new MenuDataProvider(getItems(), getItemLimit())), null);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_CELL_BROWSER)) {
      w = new BeeCellBrowser(new MenuTreeViewModel(
          new MenuDataProvider(entries), new MenuDataProvider(getItems(),
              getItemLimit())), null);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_LIST)) {
      w = new BeeListBox();
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_ORDERED_LIST)) {
      w = new BeeHtmlList(true);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_UNORDERED_LIST)) {
      w = new BeeHtmlList();
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_DEFINITION_LIST)) {
      w = new BeeDefinitionList();

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_CELL_LIST)) {
      w = new BeeCellList<MenuEntry>(new MenuCell());

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_RADIO_HOR)) {
      w = new BeeMenuBar(false, barType, BeeWidget.RADIO);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_RADIO_VERT)) {
      w = new BeeMenuBar(true, barType, BeeWidget.RADIO);

    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_BUTTONS_HOR)) {
      w = new BeeMenuBar(false, barType, BeeWidget.BUTTON);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_BUTTONS_VERT)) {
      w = new BeeMenuBar(true, barType, BeeWidget.BUTTON);

    } else {
      w = new BeeMenuBar();
    }

    return w;
  }

  private BeeMenuBar.BAR_TYPE getBarType(int idx) {
    Assert.isIndex(getBarTypes(), idx);
    return getBarTypes().get(idx);
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

  private int getRootCount() {
    return (getRoots() == null) ? 0 : getRoots().size();
  }

  @SuppressWarnings("unchecked")
  private void prepareWidget(Widget w, List<MenuEntry> entries) {
    if (w instanceof BeeListBox) {
      ((BeeListBox) w).setAllVisible();

    } else if (w instanceof BeeCellList) {
      int cnt = BeeUtils.length(entries);
      if (cnt > 0) {
        ((BeeCellList<MenuEntry>) w).setRowData(0, entries);
        ((BeeCellList<MenuEntry>) w).setRowCount(cnt);
      }
    }
  }

}
