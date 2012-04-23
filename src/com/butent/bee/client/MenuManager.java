package com.butent.bee.client;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Stack;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.menu.MenuBar;
import com.butent.bee.client.menu.MenuCommand;
import com.butent.bee.client.menu.MenuDataProvider;
import com.butent.bee.client.menu.MenuItem;
import com.butent.bee.client.menu.MenuSelectionHandler;
import com.butent.bee.client.menu.MenuSeparator;
import com.butent.bee.client.menu.MenuTreeViewModel;
import com.butent.bee.client.tree.BeeCellBrowser;
import com.butent.bee.client.tree.BeeCellTree;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.BeeDuration;
import com.butent.bee.client.widget.BeeCellList;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.menu.MenuUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * creates and manages menu of the system using authorization and layout configuration.
 */

public class MenuManager implements Module {
  
  public interface MenuCallback {
    void onSelection(String parameters);
  }

  private class DrawCommand extends BeeCommand {
    @Override
    public void execute() {
      drawMenu();
    }
  }

  private final Map<String, MenuCallback> menuCallbacks = Maps.newHashMap();
  
  private List<MenuEntry> roots = null;
  private List<MenuEntry> items = null;

  private boolean loaded = false;
  private BeeCommand onLoad = null;

  private List<String> layouts = new ArrayList<String>();
  private boolean[] options = null;

  public MenuManager() {
    super();
  }

  public boolean drawMenu() {
    if (!isLoaded()) {
      setOnLoad(new DrawCommand());
      loadMenu();
      return false;
    }
    Assert.state(validState());

    layouts.clear();
    options = new boolean[MenuConstants.MAX_MENU_DEPTH];

    for (int i = MenuConstants.ROOT_MENU_INDEX; i < MenuConstants.MAX_MENU_DEPTH; i++) {
      layouts.add(Global.getVarValue(MenuConstants.varMenuLayout(i)));
      options[i] = Global.getVarBoolean(MenuConstants.varMenuBarType(i));
    }

    Widget w = createMenu(0, getRoots(), null);

    boolean ok = (w != null);
    if (ok) {
      BeeKeeper.getScreen().updateMenu(w);
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

  public MenuCallback getMenuCallback(String service) {
    Assert.notEmpty(service);
    return menuCallbacks.get(BeeUtils.normalize(service));
  }
  
  public String getName() {
    return getClass().getName();
  }

  public BeeCommand getOnLoad() {
    return onLoad;
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

  public boolean isLoaded() {
    return loaded;
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
        BeeKeeper.getLog().severe("invalid menu entry", BeeUtils.bracket(i), arr.get(i));
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
    BeeKeeper.getLog().finish(dur, NameUtils.addName("roots", rc), NameUtils.addName("items", ic));

    setLoaded(true);
    if (getOnLoad() != null) {
      getOnLoad().execute();
      setOnLoad(null);
    }
  }

  public void loadMenu() {
    BeeKeeper.getRpc().makeGetRequest(Service.LOAD_MENU);
  }

  public void registerMenuCallback(String service, MenuCallback callback) {
    Assert.notEmpty(service);
    menuCallbacks.put(BeeUtils.normalize(service), callback);
  }
  
  public void setItems(List<MenuEntry> items) {
    this.items = items;
  }

  public void setLayouts(List<String> layouts) {
    this.layouts = layouts;
  }

  public void setLoaded(boolean loaded) {
    this.loaded = loaded;
  }

  public void setOnLoad(BeeCommand onLoad) {
    this.onLoad = onLoad;
  }

  public void setRoots(List<MenuEntry> roots) {
    this.roots = roots;
  }

  public void showMenu() {
    if (BeeUtils.allEmpty(getRoots(), getItems())) {
      Global.showDialog("menu empty");
      return;
    }

    String[] cols = new String[] {
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
    BeeKeeper.getScreen().showGrid(arr, cols);
  }

  public void start() {
    clear();
  }

  public boolean validState() {
    return BeeUtils.allNotEmpty(getRoots(), getItems());
  }

  private void addEntry(Widget rw, int itemCnt, MenuEntry item, Widget cw) {
    String txt = item.getText();
    String svc = item.getService();
    String opt = item.getParameters();

    boolean sepBefore = MenuConstants.isSeparatorBefore(item.getSeparators());
    boolean sepAfter = MenuConstants.isSeparatorAfter(item.getSeparators());

    if (rw instanceof MenuBar) {
      MenuBar mb = (MenuBar) rw;
      if (sepBefore) {
        mb.addSeparator(new MenuSeparator());
      }

      if (cw == null) {
        mb.addItem(txt, new MenuCommand(svc, opt));
      } else if (cw instanceof MenuBar) {
        mb.addItem(txt, (MenuBar) cw);
      }

      if (sepAfter) {
        mb.addSeparator(new MenuSeparator());
      }

    } else if (rw instanceof Stack) {
      if (cw != null) {
        double header = BeeUtils.rescale(itemCnt, 10, 30, 25, 15);
        ((Stack) rw).add(cw, txt, Math.ceil(header));
      }
    } else if (rw instanceof TabbedPages) {
      if (cw != null) {
        ((TabbedPages) rw).add(cw, txt);
      }

    } else if (rw instanceof Tree) {
      TreeItem it = new TreeItem(txt);

      if (cw == null) {
        it.setUserObject(new MenuCommand(svc, opt));
      } else {
        it.addItem(cw);
      }

      ((Tree) rw).addItem(it);
    }
  }

  private void clear() {
    roots = new ArrayList<MenuEntry>();
    items = new ArrayList<MenuEntry>();
  }

  private Widget createMenu(int level, List<MenuEntry> entries, Widget parent) {
    Assert.betweenExclusive(level, MenuConstants.ROOT_MENU_INDEX, MenuConstants.MAX_MENU_DEPTH);
    Assert.notEmpty(entries);

    String layout = getLayout(level);
    boolean opt = getOption(level);

    if (parent instanceof MenuBar) {
      int lvl = level - 1;
      while (lvl >= MenuConstants.ROOT_MENU_INDEX
          && BeeUtils.same(layout, MenuConstants.LAYOUT_TREE)) {
        layout = getLayout(lvl--);
      }
    }
    Widget rw = createWidget(layout, opt, entries, level);

    boolean lastLevel = (level >= MenuConstants.MAX_MENU_DEPTH - 1);

    List<MenuEntry> children = null;
    Widget cw = null;

    int cnt = entries.size();

    for (MenuEntry entry : entries) {
      if (!lastLevel) {
        children = MenuUtils.getChildren(getItems(), entry.getId(), true);
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

  private Widget createWidget(String layout, boolean opt, List<MenuEntry> entries, int level) {
    Widget w = null;

    if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_HOR)) {
      w = new MenuBar(level, false, getBarType(false), MenuItem.ITEM_TYPE.LABEL, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_VERT)) {
      w = new MenuBar(level, true, getBarType(opt), MenuItem.ITEM_TYPE.LABEL, opt);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_STACK)) {
      w = new Stack(Unit.PX);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_TAB)) {
      w = new TabbedPages();

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_TREE)) {
      w = new Tree();
      ((Tree) w).addSelectionHandler(new MenuSelectionHandler());

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_CELL_TREE)) {
      w = new BeeCellTree(new MenuTreeViewModel(new MenuDataProvider(entries),
          new MenuDataProvider(getItems())), null);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_CELL_BROWSER)) {
      w = new BeeCellBrowser(new MenuTreeViewModel(new MenuDataProvider(entries),
          new MenuDataProvider(getItems())), null);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_LIST)) {
      w = new MenuBar(level, true, MenuBar.BAR_TYPE.LIST, MenuItem.ITEM_TYPE.OPTION, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_ORDERED_LIST)) {
      w = new MenuBar(level, true, MenuBar.BAR_TYPE.OLIST, MenuItem.ITEM_TYPE.LI, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_UNORDERED_LIST)) {
      w = new MenuBar(level, true, MenuBar.BAR_TYPE.ULIST, MenuItem.ITEM_TYPE.LI, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_DEFINITION_LIST)) {
      w = new MenuBar(level, true, MenuBar.BAR_TYPE.DLIST, MenuItem.ITEM_TYPE.DT, opt);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_RADIO_HOR)) {
      w = new MenuBar(level, false, getBarType(opt), MenuItem.ITEM_TYPE.RADIO, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_RADIO_VERT)) {
      w = new MenuBar(level, true, getBarType(opt), MenuItem.ITEM_TYPE.RADIO, opt);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_BUTTONS_HOR)) {
      w = new MenuBar(level, false, getBarType(opt), MenuItem.ITEM_TYPE.BUTTON, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_BUTTONS_VERT)) {
      w = new MenuBar(level, true, getBarType(opt), MenuItem.ITEM_TYPE.BUTTON, opt);

    } else {
      Assert.untouchable();
    }

    return w;
  }

  private MenuBar.BAR_TYPE getBarType(boolean opt) {
    return opt ? MenuBar.BAR_TYPE.TABLE : MenuBar.BAR_TYPE.FLOW;
  }

  private int getItemCount() {
    return (getItems() == null) ? 0 : getItems().size();
  }

  private String getLayout(int idx) {
    Assert.isIndex(getLayouts(), idx);
    return getLayouts().get(idx);
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
    if (w instanceof MenuBar) {
      ((MenuBar) w).prepare();
    } else if (w instanceof BeeCellList) {
      int cnt = BeeUtils.length(entries);
      if (cnt > 0) {
        ((BeeCellList<MenuEntry>) w).setRowData(0, entries);
        ((BeeCellList<MenuEntry>) w).setRowCount(cnt);
      }
    }
  }
}
