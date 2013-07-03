package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.menu.MenuBar;
import com.butent.bee.client.menu.MenuCommand;
import com.butent.bee.client.menu.MenuSelectionHandler;
import com.butent.bee.client.menu.MenuSeparator;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.menu.MenuConstants.BAR_TYPE;
import com.butent.bee.shared.menu.MenuConstants.ITEM_TYPE;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;

/**
 * creates and manages menu of the system using authorization and layout configuration.
 */

public class MenuManager implements Module {

  public interface MenuCallback {
    void onSelection(String parameters);
  }

  private static final BeeLogger logger = LogUtils.getLogger(MenuManager.class);

  private final Map<String, MenuCallback> menuCallbacks = Maps.newHashMap();

  private final List<Menu> roots = Lists.newArrayList();

  private final List<String> layouts = Lists.newArrayList();

  public MenuManager() {
    super();

    layouts.add(MenuConstants.LAYOUT_MENU_HOR);
    for (int i = MenuConstants.ROOT_MENU_INDEX + 1; i < MenuConstants.MAX_MENU_DEPTH; i++) {
      layouts.add(MenuConstants.LAYOUT_MENU_VERT);
    }
  }

  public boolean drawMenu() {
    IdentifiableWidget w = createMenu(0, roots, null);
    boolean ok = (w != null);

    if (ok) {
      BeeKeeper.getScreen().updateMenu(w);
    } else {
      logger.severe("error creating menu");
    }
    return ok;
  }

  public List<String> getLayouts() {
    return layouts;
  }

  public MenuCallback getMenuCallback(String service) {
    Assert.notEmpty(service);
    return menuCallbacks.get(BeeUtils.normalize(service));
  }

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
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

  @Override
  public void init() {
  }

  public boolean loadMenu() {
    BeeKeeper.getRpc().makeGetRequest(Service.LOAD_MENU, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse()) {
          restore((String) response.getResponse());
        }
      }
    });
    return true;
  }

  @Override
  public void onExit() {
  }

  public void registerMenuCallback(String service, MenuCallback callback) {
    Assert.notEmpty(service);
    menuCallbacks.put(BeeUtils.normalize(service), callback);
  }

  public void restore(String data) {
    if (!BeeUtils.isEmpty(data)) {
      String[] arr = Codec.beeDeserializeCollection(data);

      roots.clear();
      for (String s : arr) {
        roots.add(Menu.restore(s));
      }
      
      int size = 0;
      for (Menu menu : roots) {
        size += menu.getSize();
      }
      
      logger.info("menu", size);
      
      drawMenu();
    }
  }

  public void showMenuInfo() {
    if (roots.isEmpty()) {
      Global.showInfo(Lists.newArrayList("menu empty"));
      return;
    }
    Tree tree = new Tree();

    for (Menu menu : roots) {
      TreeItem item = new TreeItem(menu.getLabel());
      collectMenuInfo(item, menu);
      tree.addItem(item);
    }
    BeeKeeper.getScreen().updateActivePanel(tree);
  }

  @Override
  public void start() {
  }

  private void addEntry(IdentifiableWidget rw, Menu item, IdentifiableWidget cw) {
    String txt = LocaleUtils.maybeLocalize(item.getLabel());
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
      if (sepBefore && mb.getItemCount() > 0) {
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

    } else if (rw instanceof Tree) {
      TreeItem it = new TreeItem(txt);

      if (cw == null) {
        it.setUserObject(new MenuCommand(svc, opt));
      } else {
        it.addItem(cw.asWidget());
      }

      ((Tree) rw).addItem(it);
    }
  }

  private void collectMenuInfo(TreeItem treeItem, Menu menu) {
    treeItem.addItem("Name: " + menu.getName());

    if (menu.getOrder() != null) {
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

  private IdentifiableWidget createMenu(int level, List<Menu> entries, IdentifiableWidget parent) {
    Assert.betweenExclusive(level, MenuConstants.ROOT_MENU_INDEX, MenuConstants.MAX_MENU_DEPTH);

    if (BeeUtils.isEmpty(entries)) {
      return null;
    }
    String layout = getLayout(level);

    if (parent instanceof MenuBar) {
      int lvl = level - 1;
      while (lvl >= MenuConstants.ROOT_MENU_INDEX
          && BeeUtils.same(layout, MenuConstants.LAYOUT_TREE)) {
        layout = getLayout(lvl--);
      }
    }
    IdentifiableWidget rw = createWidget(layout, level);

    boolean lastLevel = (level >= MenuConstants.MAX_MENU_DEPTH - 1);

    for (Menu entry : entries) {
      List<Menu> children = null;
      IdentifiableWidget cw = null;

      if (!lastLevel && (entry instanceof MenuEntry)) {
        children = ((MenuEntry) entry).getItems();
      }
      if (!BeeUtils.isEmpty(children)) {
        cw = createMenu(level + 1, children, rw);
      }
      addEntry(rw, entry, cw);
    }

    prepareWidget(rw);
    return rw;
  }

  private IdentifiableWidget createWidget(String layout, int level) {
    IdentifiableWidget w = null;

    if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_HOR)) {
      w = new MenuBar(level, false, getBarType(false), ITEM_TYPE.LABEL);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_VERT)) {
      w = new MenuBar(level, true, getBarType(true), ITEM_TYPE.LABEL);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_TREE)) {
      w = new Tree();
      ((Tree) w).addSelectionHandler(new MenuSelectionHandler());

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_LIST)) {
      w = new MenuBar(level, true, BAR_TYPE.LIST, ITEM_TYPE.OPTION);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_ORDERED_LIST)) {
      w = new MenuBar(level, true, BAR_TYPE.OLIST, ITEM_TYPE.LI);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_UNORDERED_LIST)) {
      w = new MenuBar(level, true, BAR_TYPE.ULIST, ITEM_TYPE.LI);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_DEFINITION_LIST)) {
      w = new MenuBar(level, true, BAR_TYPE.DLIST, ITEM_TYPE.DT);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_RADIO_HOR)) {
      w = new MenuBar(level, false, getBarType(true), ITEM_TYPE.RADIO);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_RADIO_VERT)) {
      w = new MenuBar(level, true, getBarType(true), ITEM_TYPE.RADIO);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_BUTTONS_HOR)) {
      w = new MenuBar(level, false, getBarType(true), ITEM_TYPE.BUTTON);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_BUTTONS_VERT)) {
      w = new MenuBar(level, true, getBarType(true), ITEM_TYPE.BUTTON);

    } else {
      Assert.untouchable();
    }

    return w;
  }

  private BAR_TYPE getBarType(boolean table) {
    return table ? BAR_TYPE.TABLE : BAR_TYPE.FLOW;
  }

  private String getLayout(int idx) {
    Assert.isIndex(getLayouts(), idx);
    return getLayouts().get(idx);
  }

  private void prepareWidget(IdentifiableWidget w) {
    if (w instanceof MenuBar) {
      ((MenuBar) w).prepare();
    }
  }
}
