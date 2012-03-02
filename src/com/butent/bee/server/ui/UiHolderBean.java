package com.butent.bee.server.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.server.Config;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.io.NameUtils;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.ui.UiLoader;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import org.w3c.dom.Document;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
    GRID("grids"), FORM("forms"), MENU("menus");

    private String path;

    private UiObject(String path) {
      this.path = path;
    }

    public String getFileName(String objName) {
      Assert.notEmpty(objName);
      return BeeUtils.concat(".", objName, name().toLowerCase(), XmlUtils.defaultXmlExtension);
    }

    public String getPath() {
      return path;
    }

    public String getSchemaPath() {
      return Config.getSchemaPath(name().toLowerCase() + ".xsd");
    }
  }

  private static Logger logger = Logger.getLogger(UiHolderBean.class.getName());

  @EJB
  ModuleHolderBean moduleBean;
  @EJB
  GridLoaderBean gridBean;
  @EJB
  UiLoaderBean loaderBean;

  UiLoader loader;
  Map<String, UiComponent> uiFormCache = new HashMap<String, UiComponent>();

  Map<String, String> gridCache = new HashMap<String, String>();
  Map<String, String> formCache = new HashMap<String, String>();
  Map<String, String> menuCache = new HashMap<String, String>();

  public ResponseObject getForm(String formName) {
    Assert.state(isForm(formName), "Not a form: " + formName);
    String resource = formCache.get(key(formName));

    Document doc = XmlUtils.getXmlResource(resource, UiObject.FORM.getSchemaPath());

    if (doc == null) {
      return ResponseObject.error("Cannot parse xml:", resource);
    }
    return ResponseObject.response(XmlUtils.toString(doc, false));
  }

  public GridDescription getGrid(String gridName) {
    Assert.state(isGrid(gridName), "Not a grid: " + gridName);
    String resource = gridCache.get(key(gridName));

    GridDescription grid = gridBean.loadGrid(resource, UiObject.GRID.getSchemaPath());

    if (grid != null) {
      if (!BeeUtils.same(grid.getName(), gridName)) {
        LogUtils.warning(logger, "Grid name doesn't match resource name:", gridName, resource);
      } else if (grid.isEmpty()) {
        LogUtils.warning(logger, resource, "Grid has no columns defined:", gridName);
      } else {
        return grid;
      }
    }
    return null;
  }

  public UiComponent getUiForm(String root, Object... params) {
    Assert.notEmpty(root);

    if (!uiFormCache.containsKey(root)) {
      loadUiForm(root, params);
    }
    return uiFormCache.get(root);
  }

  public UiComponent getUiMenu(String root, Object... params) {
    Assert.notEmpty(root);
    return loader.getMenu(root, params);
  }

  @Lock(LockType.WRITE)
  public void initForms() {
    initObjects(UiObject.FORM);
  }

  @Lock(LockType.WRITE)
  public void initGrids() {
    initObjects(UiObject.GRID);
  }

  public boolean isForm(String formName) {
    return !BeeUtils.isEmpty(formName) && (formCache.containsKey(key(formName)));
  }

  public boolean isGrid(String gridName) {
    return !BeeUtils.isEmpty(gridName) && (gridCache.containsKey(key(gridName)));
  }

  public void setUiLoader(UiLoader loader) {
    this.loader = loader;
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    setUiLoader(loaderBean);

    initGrids();
    initForms();
  }

  private boolean initForm(String moduleName, String formName) {
    Assert.notEmpty(formName);

    String resource = Config.getPath(moduleBean.getResourcePath(moduleName,
        UiObject.FORM.getPath(), UiObject.FORM.getFileName(formName)));

    boolean ok = !BeeUtils.isEmpty(resource);

    if (ok) {
      register(resource, formCache, formName, moduleName);
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
      register(resource, gridCache, gridName, moduleName);
    } else {
      unregister(gridName, gridCache);
    }
    return ok;
  }

  private void initObjects(UiObject obj) {
    Assert.notEmpty(obj);

    switch (obj) {
      case GRID:
        gridCache.clear();
        break;
      case FORM:
        formCache.clear();
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
      root = new File(Config.USER_DIR, modulePath);
      if (FileUtils.isDirectory(root)) {
        roots.add(root);
      }
      List<File> resources =
          FileUtils.findFiles(obj.getFileName("*"), roots, null, null, false, true);

      if (!BeeUtils.isEmpty(resources)) {
        Set<String> objects = Sets.newHashSet();

        for (File resource : resources) {
          String resourcePath = resource.getPath();
          String objectName = NameUtils.getBaseName(resourcePath);
          objectName = objectName.substring(0, objectName.length() - obj.name().length() - 1);
          objects.add(key(objectName));
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
              // isOk = initMenu(moduleName, objectName);
              break;
          }
          if (isOk) {
            cnt++;
          }
        }
      }
    }
    if (BeeUtils.isEmpty(cnt)) {
      LogUtils.severe(logger, "No", obj.name(), "descriptions found");
    } else {
      LogUtils.infoNow(logger, "Loaded", cnt, obj.name(), "descriptions");
    }
  }

  private String key(String name) {
    return BeeUtils.normalize(name);
  }

  private void loadUiForm(String root, Object... params) {
    UiComponent form = loader.getForm(root, params);
    uiFormCache.put(root, form);
  }

  private <T> void register(T object, Map<String, T> cache, String objectName,
      String moduleName) {
    if (!BeeUtils.isEmpty(object)) {
      String name = BeeUtils.getClassName(object.getClass());

      if (cache.containsKey(key(objectName))) {
        LogUtils.warning(logger, BeeUtils.parenthesize(moduleName),
            "Dublicate", name, "name:", BeeUtils.bracket(objectName));
      } else {
        cache.put(key(objectName), object);
        LogUtils.info(logger, BeeUtils.parenthesize(moduleName),
            "Registered", name, BeeUtils.bracket(objectName));
      }
    }
  }

  private void unregister(String objectName, Map<String, ?> cache) {
    if (!BeeUtils.isEmpty(objectName)) {
      cache.remove(key(objectName));
    }
  }
}
