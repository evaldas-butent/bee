package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.layout.Stack;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.menu.MenuBar;
import com.butent.bee.client.menu.MenuCommand;
import com.butent.bee.client.menu.MenuSelectionHandler;
import com.butent.bee.client.menu.MenuSeparator;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.widget.BeeCellList;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.menu.MenuConstants.BAR_TYPE;
import com.butent.bee.shared.menu.MenuConstants.ITEM_TYPE;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

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

  private final Map<String, MenuCallback> menuCallbacks = Maps.newHashMap();

  private List<Menu> roots = null;

  private final List<String> layouts = Lists.newArrayList();
  private final List<Boolean> options = Lists.newArrayList();

  public MenuManager() {
    super();

    layouts.add(MenuConstants.LAYOUT_MENU_HOR);
    options.add(false);

    for (int i = MenuConstants.ROOT_MENU_INDEX + 1; i < MenuConstants.MAX_MENU_DEPTH; i++) {
      layouts.add(MenuConstants.LAYOUT_MENU_VERT);
      options.add(true);
    }
  }

  public boolean drawMenu() {
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

  public List<Menu> getRoots() {
    return roots;
  }

  public void init() {
  }

  public boolean loadMenu() {
    BeeKeeper.getRpc().makeGetRequest(Service.LOAD_MENU, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse()) {
          roots = Lists.newArrayList();
          String[] arr = Codec.beeDeserializeCollection((String) response.getResponse());

          for (String s : arr) {
            roots.add(Menu.restore(s));
          }
          drawMenu();
        }
      }
    });
    return true;
  }

  public void registerMenuCallback(String service, MenuCallback callback) {
    Assert.notEmpty(service);
    menuCallbacks.put(BeeUtils.normalize(service), callback);
  }

  public void showMenuInfo() {
    if (BeeUtils.isEmpty(getRoots())) {
      Global.showDialog("menu empty");
      return;
    }
    Tree tree = new Tree();

    for (Menu menu : getRoots()) {
      TreeItem item = new TreeItem(menu.getLabel());
      collectMenuInfo(item, menu);
      tree.addItem(item);
    }
    BeeKeeper.getScreen().updateActivePanel(tree);
  }

  public void start() {
    clear();
  }

  private void addEntry(Widget rw, int itemCnt, Menu item, Widget cw) {
    String txt = item.getLabel();
    String svc = null;
    String opt = null;

    if (item instanceof MenuItem) {
      svc = ((MenuItem) item).getService();
      opt = ((MenuItem) item).getParameters();
    }
    boolean sepBefore = item.hasSeparator();
    boolean sepAfter = false;

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
    roots = new ArrayList<Menu>();
  }

  private void collectMenuInfo(TreeItem treeItem, Menu menu) {
    treeItem.addItem("Name: " + menu.getName());

    if (!BeeUtils.isEmpty(menu.getOrder())) {
      treeItem.addItem("Order: " + menu.getOrder());
    }
    if (menu.hasSeparator()) {
      treeItem.addItem("Separator: true");
    }
    if (menu instanceof MenuItem) {
      MenuItem item = (MenuItem) menu;
      treeItem.addItem("Service: " + item.getService());

      if (!BeeUtils.isEmpty(item.getParameters())) {
        treeItem.addItem("Parameters: " + item.getParameters());
      }
    } else if (menu instanceof MenuEntry) {
      TreeItem cc = new TreeItem("Items");

      for (Menu item : ((MenuEntry) menu).getItems()) {
        TreeItem itm = new TreeItem(item.getLabel());
        collectMenuInfo(itm, item);
        cc.addItem(itm);
      }
      treeItem.addItem(cc);
    }
  }

  private Widget createMenu(int level, List<Menu> entries, Widget parent) {
    Assert.betweenExclusive(level, MenuConstants.ROOT_MENU_INDEX, MenuConstants.MAX_MENU_DEPTH);

    if (BeeUtils.isEmpty(entries)) {
      return null;
    }
    String layout = getLayout(level);
    boolean opt = getOption(level);

    if (parent instanceof MenuBar) {
      int lvl = level - 1;
      while (lvl >= MenuConstants.ROOT_MENU_INDEX
          && BeeUtils.same(layout, MenuConstants.LAYOUT_TREE)) {
        layout = getLayout(lvl--);
      }
    }
    Widget rw = createWidget(layout, opt, level);

    boolean lastLevel = (level >= MenuConstants.MAX_MENU_DEPTH - 1);
    int cnt = entries.size();

    for (Menu entry : entries) {
      List<Menu> children = null;
      Widget cw = null;

      if (!lastLevel && (entry instanceof MenuEntry)) {
        children = ((MenuEntry) entry).getItems();
      }
      if (!BeeUtils.isEmpty(children)) {
        cw = createMenu(level + 1, children, rw);
      }
      addEntry(rw, cnt, entry, cw);
    }

    prepareWidget(rw, entries);
    return rw;
  }

  private Widget createWidget(String layout, boolean opt, int level) {
    Widget w = null;

    if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_HOR)) {
      w = new MenuBar(level, false, getBarType(false), ITEM_TYPE.LABEL, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_VERT)) {
      w = new MenuBar(level, true, getBarType(opt), ITEM_TYPE.LABEL, opt);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_STACK)) {
      w = new Stack(Unit.PX);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_TAB)) {
      w = new TabbedPages();

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_TREE)) {
      w = new Tree();
      ((Tree) w).addSelectionHandler(new MenuSelectionHandler());

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_LIST)) {
      w = new MenuBar(level, true, BAR_TYPE.LIST, ITEM_TYPE.OPTION, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_ORDERED_LIST)) {
      w = new MenuBar(level, true, BAR_TYPE.OLIST, ITEM_TYPE.LI, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_UNORDERED_LIST)) {
      w = new MenuBar(level, true, BAR_TYPE.ULIST, ITEM_TYPE.LI, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_DEFINITION_LIST)) {
      w = new MenuBar(level, true, BAR_TYPE.DLIST, ITEM_TYPE.DT, opt);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_RADIO_HOR)) {
      w = new MenuBar(level, false, getBarType(opt), ITEM_TYPE.RADIO, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_RADIO_VERT)) {
      w = new MenuBar(level, true, getBarType(opt), ITEM_TYPE.RADIO, opt);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_BUTTONS_HOR)) {
      w = new MenuBar(level, false, getBarType(opt), ITEM_TYPE.BUTTON, opt);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_BUTTONS_VERT)) {
      w = new MenuBar(level, true, getBarType(opt), ITEM_TYPE.BUTTON, opt);

    } else {
      Assert.untouchable();
    }

    return w;
  }

  private BAR_TYPE getBarType(boolean opt) {
    return opt ? BAR_TYPE.TABLE : BAR_TYPE.FLOW;
  }

  private String getLayout(int idx) {
    Assert.isIndex(getLayouts(), idx);
    return getLayouts().get(idx);
  }

  private boolean getOption(int idx) {
    Assert.isIndex(options, idx);
    return options.get(idx);
  }

  @SuppressWarnings("unchecked")
  private void prepareWidget(Widget w, List<Menu> entries) {
    if (w instanceof MenuBar) {
      ((MenuBar) w).prepare();
    } else if (w instanceof BeeCellList) {
      int cnt = BeeUtils.length(entries);
      if (cnt > 0) {
        ((BeeCellList<Menu>) w).setRowData(0, entries);
        ((BeeCellList<Menu>) w).setRowCount(cnt);
      }
    }
  }
}
