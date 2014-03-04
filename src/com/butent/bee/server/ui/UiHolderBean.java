package com.butent.bee.server.ui;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.menu.MenuService.DataNameProvider;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.RightsUtils;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import org.w3c.dom.Document;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 * Initializes and uses <code>UiLoaderBean</code> for dynamic interface generation from a data
 * source.
 */

@Singleton
@Lock(LockType.READ)
public class UiHolderBean {

  public enum UiObject {
    GRID("grids"), FORM("forms"), MENU("menu");

    private final String path;

    private UiObject(String path) {
      this.path = path;
    }

    public String getFileName(String objName) {
      Assert.notEmpty(objName);
      return BeeUtils.join(".", objName, name().toLowerCase(), XmlUtils.DEFAULT_XML_EXTENSION);
    }

    public String getPath() {
      return path;
    }

    public String getSchemaPath() {
      return Config.getSchemaPath(name().toLowerCase() + ".xsd");
    }
  }

  private static BeeLogger logger = LogUtils.getLogger(UiHolderBean.class);

  private static String key(String name) {
    return BeeUtils.normalize(name);
  }

  private static <T> void register(String clazz, T object, Map<String, T> cache, String objectName,
      String moduleName) {

    if (object != null) {
      if (cache.containsKey(key(objectName))) {
        logger.warning(BeeUtils.parenthesize(moduleName),
            "Dublicate", clazz, "name:", BeeUtils.bracket(objectName));
      } else {
        cache.put(key(objectName), object);
      }
    }
  }

  private static void unregister(String objectName, Map<String, ?> cache) {
    if (!BeeUtils.isEmpty(objectName)) {
      cache.remove(key(objectName));
    }
  }

  @EJB
  ModuleHolderBean moduleBean;
  @EJB
  GridLoaderBean gridBean;
  @EJB
  UserServiceBean usr;

  private final Map<String, String> gridCache = Maps.newHashMap();

  private final Map<String, String> formCache = Maps.newHashMap();

  private final Map<String, Menu> menuCache = Maps.newHashMap();

  private final Map<String, String> gridViewNames = new ConcurrentHashMap<>();
  private final Map<String, String> formViewNames = new ConcurrentHashMap<>();

  public ResponseObject getForm(String formName) {
    if (!isForm(formName)) {
      return ResponseObject.error("Not a form:", formName);
    }

    String resource = formCache.get(key(formName));
    Document doc = XmlUtils.getXmlResource(resource, UiObject.FORM.getSchemaPath());

    if (doc == null) {
      return ResponseObject.error("Cannot parse xml:", resource);
    }
    return ResponseObject.response(XmlUtils.toString(doc, false));
  }

  public GridDescription getGrid(String gridName) {
    if (!isGrid(gridName)) {
      logger.severe("Not a grid:", gridName);
      return null;
    }

    String resource = gridCache.get(key(gridName));
    GridDescription grid = gridBean.loadGrid(resource, UiObject.GRID.getSchemaPath());

    if (grid != null) {
      if (!BeeUtils.same(grid.getName(), gridName)) {
        logger.warning("Grid name doesn't match resource name:", gridName, resource);
      } else if (grid.isEmpty()) {
        logger.warning(resource, "Grid has no columns defined:", gridName);
      } else {
        return grid;
      }
    }
    return null;
  }

  public ResponseObject getMenu(boolean checkRights) {
    Map<Integer, Menu> menus = Maps.newTreeMap();

    for (Menu menu : menuCache.values()) {
      Menu entry = getMenu(null, Menu.restore(Codec.beeSerialize(menu)), checkRights);

      if (entry != null) {
        menus.put(entry.getOrder(), entry);
      }
    }
    return ResponseObject.response(menus.values());
  }

  @Lock(LockType.WRITE)
  public void initForms() {
    initObjects(UiObject.FORM);
  }

  @Lock(LockType.WRITE)
  public void initGrids() {
    initObjects(UiObject.GRID);
  }

  @Lock(LockType.WRITE)
  public void initMenu() {
    initObjects(UiObject.MENU);

    for (String menuKey : Sets.newHashSet(menuCache.keySet())) {
      Menu xmlMenu = menuCache.get(menuKey);
      String parent = xmlMenu.getParent();

      if (!BeeUtils.isEmpty(parent)) {
        Menu menu = null;

        for (String entry : Splitter.on('.').omitEmptyStrings().trimResults().split(parent)) {
          if (menu == null) {
            menu = menuCache.get(key(entry));

          } else if (menu instanceof MenuEntry) {
            boolean found = false;

            for (Menu item : ((MenuEntry) menu).getItems()) {
              found = BeeUtils.same(item.getName(), entry);

              if (found) {
                menu = item;
                break;
              }
            }
            if (!found) {
              menu = null;
              break;
            }
          } else {
            break;
          }
        }
        if (menu == null || !(menu instanceof MenuEntry)) {
          logger.severe("Menu parent is not valid:", "Module:", xmlMenu.getModule(),
              "; Menu:", xmlMenu.getName(), "; Parent:", parent);
        } else {
          List<Menu> items = ((MenuEntry) menu).getItems();

          if (BeeUtils.isIndex(items, xmlMenu.getOrder())) {
            items.add(xmlMenu.getOrder(), xmlMenu);
          } else {
            items.add(xmlMenu);
          }
        }
        unregister(menuKey, menuCache);
      }
    }
  }

  public boolean isForm(String formName) {
    return !BeeUtils.isEmpty(formName) && (formCache.containsKey(key(formName)));
  }

  public boolean isGrid(String gridName) {
    return !BeeUtils.isEmpty(gridName) && (gridCache.containsKey(key(gridName)));
  }

  public Menu loadXmlMenu(String resource) {
    return XmlUtils.unmarshal(Menu.class, resource, UiObject.MENU.getSchemaPath());
  }

  private String getFormViewName(String formName) {
    if (formViewNames.containsKey(formName)) {
      return formViewNames.get(formName);
    }

    String viewName = null;
    String resource = formCache.get(key(formName));

    if (!BeeUtils.isEmpty(resource)) {
      Document doc = XmlUtils.getXmlResource(resource, UiObject.FORM.getSchemaPath());
      if (doc != null) {
        viewName = doc.getDocumentElement().getAttribute(UiConstants.ATTR_VIEW_NAME);
      }
    }
    
    formViewNames.put(formName, Strings.nullToEmpty(viewName));
    return viewName;
  }

  private String getGridViewName(String gridName) {
    if (gridViewNames.containsKey(gridName)) {
      return gridViewNames.get(gridName);
    }

    String viewName = null;
    String resource = gridCache.get(key(gridName));

    if (!BeeUtils.isEmpty(resource)) {
      Document doc = XmlUtils.getXmlResource(resource, UiObject.GRID.getSchemaPath());
      if (doc != null) {
        viewName = doc.getDocumentElement().getAttribute(UiConstants.ATTR_VIEW_NAME);
      }
    }
    
    gridViewNames.put(gridName, Strings.nullToEmpty(viewName));
    return viewName;
  }

  private boolean hasMenuDataRights(MenuItem item) {
    MenuService service = item.getService();

    if (service != null) {
      String parameters = item.getParameters();

      Set<String> dataNames = service.getDataNames(parameters);
      Set<RightsState> states = service.getDataRightsStates();

      if (!BeeUtils.isEmpty(dataNames) && !BeeUtils.isEmpty(states)) {
        for (String dataName : dataNames) {
          for (RightsState state : states) {
            if (!usr.hasDataRight(dataName, state)) {
              return false;
            }
          }
        }
      }
    }

    return true;
  }

  private Menu getMenu(String parent, Menu entry, boolean checkRights) {
    String ref = RightsUtils.JOINER.join(parent, entry.getName());

    boolean visible;
    if (checkRights) {
      visible = usr.isModuleVisible(entry.getModule()) && usr.isMenuVisible(ref);
      if (visible && entry instanceof MenuItem) {
        visible = hasMenuDataRights((MenuItem) entry);
      }

    } else {
      visible = Module.isEnabled(entry.getModule());
    }

    if (visible) {
      if (entry instanceof MenuEntry) {
        List<Menu> items = ((MenuEntry) entry).getItems();

        if (!BeeUtils.isEmpty(items)) {
          for (Iterator<Menu> iterator = items.iterator(); iterator.hasNext();) {
            if (getMenu(ref, iterator.next(), checkRights) == null) {
              iterator.remove();
            }
          }
        }
      }
      return entry;

    } else {
      return null;
    }
  }

  @PostConstruct
  private void init() {
    initGrids();
    initForms();
    initMenu();

    DataNameProvider gridDataNameProvider = new MenuService.DataNameProvider() {
      @Override
      public Set<String> apply(String input) {
        Set<String> result = new HashSet<>();
        String viewName = getGridViewName(input);
        if (!BeeUtils.isEmpty(viewName)) {
          result.add(viewName);
        }
        return result;
      }
    };

    MenuService.GRID.setDataNameProvider(gridDataNameProvider);
    MenuService.ENSURE_CATEGORIES_AND_OPEN_GRID.setDataNameProvider(gridDataNameProvider);

    MenuService.FORM.setDataNameProvider(new MenuService.DataNameProvider() {
      @Override
      public Set<String> apply(String input) {
        Set<String> result = new HashSet<>();
        String viewName = getFormViewName(input);
        if (!BeeUtils.isEmpty(viewName)) {
          result.add(viewName);
        }
        return result;
      }
    });
  }

  private boolean initForm(String moduleName, String formName) {
    Assert.notEmpty(formName);

    String resource = Config.getPath(moduleBean.getResourcePath(moduleName,
        UiObject.FORM.getPath(), UiObject.FORM.getFileName(formName)));

    boolean ok = !BeeUtils.isEmpty(resource);

    if (ok) {
      register("form", resource, formCache, formName, moduleName);
    } else {
      unregister(formName, formCache);
    }
    return ok;
  }

  private boolean initGrid(String moduleName, String gridName) {
    Assert.notEmpty(gridName);

    String resource = Config.getPath(moduleBean.getResourcePath(moduleName,
        UiObject.GRID.getPath(), UiObject.GRID.getFileName(gridName)));

    boolean ok = !BeeUtils.isEmpty(resource);

    if (ok) {
      register("grid", resource, gridCache, gridName, moduleName);
    } else {
      unregister(gridName, gridCache);
    }
    return ok;
  }

  private boolean initMenu(String moduleName, String menuName) {
    Assert.notEmpty(menuName);

    Menu xmlMenu = loadXmlMenu(Config.getPath(moduleBean.getResourcePath(moduleName,
        UiObject.MENU.getPath(), UiObject.MENU.getFileName(menuName))));

    boolean ok = xmlMenu != null;
    if (ok) {
      register("menu", xmlMenu, menuCache, BeeUtils.join(".", xmlMenu.getParent(), menuName),
          moduleName);
    } else {
      unregister(menuName, menuCache);
    }
    return ok;
  }

  private void initObjects(UiObject obj) {
    Assert.notNull(obj);

    switch (obj) {
      case GRID:
        gridCache.clear();
        gridViewNames.clear();
        break;

      case FORM:
        formCache.clear();
        formViewNames.clear();
        break;
      
      case MENU:
        menuCache.clear();
        break;
    }

    int cnt = 0;
    Collection<File> roots = Lists.newArrayList();

    for (String moduleName : moduleBean.getModules()) {
      roots.clear();
      String modulePath = moduleBean.getResourcePath(moduleName, obj.getPath());

      File root = new File(Config.CONFIG_DIR, modulePath);
      if (FileUtils.isDirectory(root)) {
        roots.add(root);
      }
      root = new File(Config.LOCAL_DIR, modulePath);
      if (FileUtils.isDirectory(root)) {
        roots.add(root);
      }
      List<File> resources =
          FileUtils.findFiles(obj.getFileName("*"), roots, null, null, false, true);

      if (!BeeUtils.isEmpty(resources)) {
        Set<String> objects = Sets.newHashSet();

        for (File resource : resources) {
          String resourcePath = resource.getPath();
          String objectName = FileNameUtils.getBaseName(resourcePath);
          objectName = objectName.substring(0, objectName.length() - obj.name().length() - 1);
          objects.add(objectName);
        }
        for (String objectName : objects) {
          boolean isOk = false;

          switch (obj) {
            case GRID:
              isOk = initGrid(moduleName, objectName);
              break;
            case FORM:
              isOk = initForm(moduleName, objectName);
              break;
            case MENU:
              isOk = initMenu(moduleName, objectName);
              break;
          }
          if (isOk) {
            cnt++;
          }
        }
      }
    }
    if (cnt == 0) {
      logger.severe("No", obj.name(), "descriptions found");
    } else {
      logger.info("Loaded", cnt, obj.name(), "descriptions");
    }
  }
}
