package com.butent.bee.server.modules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.BeeTable;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Singleton
@Lock(LockType.READ)
public class ModuleHolderBean {

  private static final String PROPERTY_MODULES = "Modules";
  private static final String MODULE_BEAN_PREFIX = "ModuleBean";

  private enum TABLE_ACTIVATION_MODE {
    DELAYED, FORCED
  }

  private static BeeLogger logger = LogUtils.getLogger(ModuleHolderBean.class);

  private final Map<String, BeeModule> modules = Maps.newLinkedHashMap();

  @EJB
  SystemBean sys;
  @EJB
  ParamHolderBean prm;

  public ResponseObject doModule(RequestInfo reqInfo) {
    Assert.notNull(reqInfo);
    return getModule(reqInfo.getService()).doService(reqInfo);
  }

  public List<SearchResult> doSearch(String query) {
    Assert.notEmpty(query);
    List<SearchResult> results = Lists.newArrayList();

    for (BeeModule module : modules.values()) {
      List<SearchResult> found = module.doSearch(query);
      if (found != null && !found.isEmpty()) {
        results.addAll(found);
      }
    }
    return results;
  }

  public Collection<BeeParameter> getModuleDefaultParameters(String moduleName) {
    return getModule(moduleName).getDefaultParameters();
  }

  public Collection<String> getModules() {
    return ImmutableSet.copyOf(modules.keySet());
  }

  public String getResourcePath(String moduleName, String... resources) {
    Assert.isTrue(!ArrayUtils.isEmpty(resources));
    String resource = ArrayUtils.join("/", resources);

    if (!BeeUtils.isEmpty(moduleName)) {
      resource = BeeUtils.join("/",
          BeeUtils.normalize(PROPERTY_MODULES), getModule(moduleName).getResourcePath(), resource);
    }
    return resource;
  }

  public boolean hasModule(String moduleName) {
    Assert.notEmpty(moduleName);
    return modules.containsKey(moduleName);
  }

  public void initModules() {
    TABLE_ACTIVATION_MODE mode = EnumUtils.getEnumByName(TABLE_ACTIVATION_MODE.class,
        Config.getProperty("TableActivationMode"));

    for (String mod : getModules()) {
      prm.refreshModuleParameters(mod);

      if (mode != TABLE_ACTIVATION_MODE.DELAYED) {
        for (String tblName : sys.getTableNames()) {
          BeeTable table = sys.getTable(tblName);

          if (BeeUtils.same(table.getModuleName(), mod)) {
            if (mode == TABLE_ACTIVATION_MODE.FORCED) {
              sys.rebuildTable(tblName);
            } else {
              sys.activateTable(tblName);
            }
          }
        }
      }
      getModule(mod).init();
    }
  }

  private BeeModule getModule(String moduleName) {
    Assert.state(hasModule(moduleName), "Unknown module name: " + moduleName);
    return modules.get(moduleName);
  }

  @PostConstruct
  private void init() {
    List<String> mods = Lists.newArrayList(CommonsConstants.COMMONS_MODULE);

    for (Module modul : Module.values()) {
      if (BeeUtils.isEmpty(modul.getName())) {
        logger.severe("Module", BeeUtils.bracket(modul.name()), "does not have name");
      } else {
        mods.add(modul.getName());
      }
    }
    for (String moduleName : mods) {
      try {
        BeeModule module = (BeeModule) InitialContext.doLookup("java:module/"
            + moduleName + MODULE_BEAN_PREFIX);

        if (hasModule(moduleName)) {
          logger.severe("Dublicate module name:", BeeUtils.bracket(moduleName));

        } else {
          modules.put(moduleName, module);
          logger.info("Registered module:", BeeUtils.bracket(moduleName));
        }
      } catch (NamingException ex) {
        logger.severe("Module not found:", BeeUtils.bracket(moduleName));

      } catch (ClassCastException ex) {
        logger.severe("Not a module:", BeeUtils.bracket(moduleName));
      }
    }
    if (modules.isEmpty()) {
      logger.warning("No modules registered");
    }
  }
}
