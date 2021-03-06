package com.butent.bee.client;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
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
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.menu.MenuConstants.BarType;
import com.butent.bee.shared.menu.MenuConstants.ItemType;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

/**
 * creates and manages menu of the system using authorization and layout configuration.
 */

public class MenuManager {

  private static final BeeLogger logger = LogUtils.getLogger(MenuManager.class);

  private static void addEntry(IdentifiableWidget rw, Menu item, IdentifiableWidget cw) {
    String txt = Localized.maybeTranslate(item.getLabel());
    MenuService svc = null;
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
        if (svc == null) {
          noService(item);
        } else {
          mb.addItem(txt, new MenuCommand(svc, opt));
        }

      } else if (cw instanceof MenuBar) {
        mb.addItem(txt, (MenuBar) cw);
      }

      if (sepAfter) {
        mb.addSeparator(new MenuSeparator());
      }

    } else if (rw instanceof Tree) {
      TreeItem it = new TreeItem(txt);

      if (cw == null) {
        if (svc == null) {
          noService(item);
        } else {
          it.setUserObject(new MenuCommand(svc, opt));
        }

      } else {
        it.addItem(cw.asWidget());
      }

      ((Tree) rw).addItem(it);
    }
  }

  private static IdentifiableWidget createWidget(String layout, int level) {
    IdentifiableWidget w = null;

    if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_HOR)) {
      w = new MenuBar(level, false, getBarType(false), ItemType.LABEL);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_MENU_VERT)) {
      w = new MenuBar(level, true, getBarType(true), ItemType.LABEL);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_TREE)) {
      w = new Tree();
      ((Tree) w).addSelectionHandler(new MenuSelectionHandler());

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_LIST)) {
      w = new MenuBar(level, true, BarType.LIST, ItemType.OPTION);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_ORDERED_LIST)) {
      w = new MenuBar(level, true, BarType.OLIST, ItemType.LI);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_UNORDERED_LIST)) {
      w = new MenuBar(level, true, BarType.ULIST, ItemType.LI);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_DEFINITION_LIST)) {
      w = new MenuBar(level, true, BarType.DLIST, ItemType.DT);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_RADIO_HOR)) {
      w = new MenuBar(level, false, getBarType(true), ItemType.RADIO);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_RADIO_VERT)) {
      w = new MenuBar(level, true, getBarType(true), ItemType.RADIO);

    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_BUTTONS_HOR)) {
      w = new MenuBar(level, false, getBarType(true), ItemType.BUTTON);
    } else if (BeeUtils.same(layout, MenuConstants.LAYOUT_BUTTONS_VERT)) {
      w = new MenuBar(level, true, getBarType(true), ItemType.BUTTON);

    } else {
      Assert.untouchable();
    }

    return w;
  }

  private static List<MenuItem> filterItems(Menu menu, MenuService service) {
    List<MenuItem> result = new ArrayList<>();

    if (menu instanceof MenuItem) {
      if (hasService(menu, service)) {
        result.add((MenuItem) menu);
      }

    } else if (menu instanceof MenuEntry) {
      for (Menu child : ((MenuEntry) menu).getItems()) {
        if (child instanceof MenuItem) {
          if (hasService(child, service)) {
            result.add((MenuItem) child);
          }
        } else {
          result.addAll(filterItems(child, service));
        }
      }
    }

    return result;
  }

  private static MenuItem findItem(Menu menu, String name) {
    if (menu instanceof MenuItem) {
      if (BeeUtils.same(menu.getName(), name)) {
        return (MenuItem) menu;
      } else {
        return null;
      }

    } else if (menu instanceof MenuEntry) {
      for (Menu child : ((MenuEntry) menu).getItems()) {
        MenuItem item = findItem(child, name);
        if (item != null) {
          return item;
        }
      }
      return null;

    } else {
      return null;
    }
  }

  private static BarType getBarType(boolean table) {
    return table ? BarType.TABLE : BarType.FLOW;
  }

  private static boolean hasService(Menu item, MenuService service) {
    return item instanceof MenuItem && service != null
        && ((MenuItem) item).getService() == service;
  }

  /**
   * Conditionally hides menu items.
   */
  private static boolean isVisible(Menu item) {
    switch (item.getName()) {
      case "RefVehicles":
      case "RefVehicleTypes":
      case "RefDrivers":
      case "RefTransportGroups":
        return !BeeKeeper.getUser().isModuleVisible(ModuleAndSub.of(Module.TRANSPORT));

      default:
        return true;
    }
  }

  private static void noService(Menu item) {
    logger.warning("service not available for menu item", item.getParent(), item.getName());
  }

  private static void prepareWidget(IdentifiableWidget w) {
    if (w instanceof MenuBar) {
      ((MenuBar) w).prepare();
    }
  }

  private final List<Menu> roots = new ArrayList<>();

  private final List<String> layouts = new ArrayList<>();

  public MenuManager() {
    super();

    layouts.add(MenuConstants.LAYOUT_MENU_HOR);
    for (int i = MenuConstants.ROOT_MENU_INDEX + 1; i < MenuConstants.MAX_MENU_DEPTH; i++) {
      layouts.add(MenuConstants.LAYOUT_MENU_VERT);
    }
  }

  public boolean refresh() {
    IdentifiableWidget w = createMenu(0, roots, null);
    boolean ok = w != null;

    if (ok) {
      BeeKeeper.getScreen().updateMenu(w);
    } else {
      logger.severe("error creating menu");
    }
    return ok;
  }

  public boolean executeItem(String name) {
    if (BeeUtils.isEmpty(name)) {
      return false;
    }

    MenuItem item = null;

    for (Menu menu : roots) {
      item = findItem(menu, name);
      if (item != null) {
        break;
      }
    }

    if (item == null) {
      logger.warning("menu item", name, "not found");
      return false;

    } else if (item.getService() == null) {
      noService(item);
      return false;

    } else {
      MenuCommand command = new MenuCommand(item.getService(), item.getParameters());
      command.execute();
      return true;
    }
  }

  public List<MenuItem> filter(MenuService service) {
    List<MenuItem> result = new ArrayList<>();

    if (service != null) {
      for (Menu menu : roots) {
        result.addAll(filterItems(menu, service));
      }
    }

    return result;
  }

  public List<MenuCommand> getCommands() {
    List<MenuCommand> commands = new ArrayList<>();

    for (Menu menu : roots) {
      commands.addAll(getCommands(menu));
    }

    return commands;
  }

  public List<String> getLayouts() {
    return layouts;
  }

  public String getRootLayout() {
    return getLayout(0);
  }

  public List<Menu> getRoots() {
    return roots;
  }

  public boolean isEmpty() {
    return roots.isEmpty();
  }

  public void loadMenu() {
    loadMenu(null);
  }

  public void loadMenu(final Runnable callback) {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_MENU);
    params.addQueryItem(Service.VAR_RIGHTS, 1);
    params.addQueryItem(Service.VAR_TRANSFORM, 1);

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse()) {
          restore(response.getResponseAsString());

          if (callback != null) {
            callback.run();
          }
        }
      }
    });
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

      refresh();
    }
  }

  public void showMenuInfo() {
    if (roots.isEmpty()) {
      Global.showInfo("menu empty");
      return;
    }
    Tree tree = new Tree();

    for (Menu menu : roots) {
      TreeItem item = new TreeItem(Localized.maybeTranslate(menu.getLabel()));
      collectMenuInfo(item, menu);
      tree.addItem(item);
    }

    BeeKeeper.getScreen().show(tree);
  }

  private static void collectMenuInfo(TreeItem treeItem, Menu menu) {
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
        TreeItem itm = new TreeItem(Localized.maybeTranslate(item.getLabel()));
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

    boolean lastLevel = level >= MenuConstants.MAX_MENU_DEPTH - 1;

    for (Menu entry : entries) {
      if (isVisible(entry)) {
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
    }

    prepareWidget(rw);
    return rw;
  }

  private static List<MenuCommand> getCommands(Menu menu) {
    List<MenuCommand> commands = new ArrayList<>();

    if (menu instanceof MenuItem) {
      MenuItem item = (MenuItem) menu;
      commands.add(new MenuCommand(item.getService(), item.getParameters()));

    } else if (menu instanceof MenuEntry) {
      for (Menu child : ((MenuEntry) menu).getItems()) {
        commands.addAll(getCommands(child));
      }
    }

    return commands;
  }

  private String getLayout(int idx) {
    Assert.isIndex(getLayouts(), idx);
    return getLayouts().get(idx);
  }
}
