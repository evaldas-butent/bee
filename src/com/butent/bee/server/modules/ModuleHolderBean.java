package com.butent.bee.server.modules;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.server.Config;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Singleton
@Lock(LockType.READ)
public class ModuleHolderBean {
  private static final Splitter SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
  private static final String PROPERTY_MODULES = "Modules";
  private static final String MODULE_BEAN_PREFIX = "ModuleBean";
  private static Logger logger = Logger.getLogger(ModuleHolderBean.class.getName());

  private final Map<String, BeeModule> modules = Maps.newHashMap();

  public ResponseObject doModule(RequestInfo reqInfo) {
    Assert.notNull(reqInfo);
    return getModule(reqInfo.getService()).doService(reqInfo);
  }

  public Collection<String> getModules() {
    return ImmutableSet.copyOf(modules.keySet());
  }

  public String getResourcePath(String moduleName, String... resources) {
    Assert.notNull(resources);
    Assert.notEmpty(resources);
    String resource = BeeUtils.concat("/", resources);

    if (!BeeUtils.isEmpty(moduleName)) {
      resource = BeeUtils.concat("/",
          PROPERTY_MODULES, getModule(moduleName).getResourcePath(), resource);
    }
    return resource;
  }

  public boolean hasModule(String moduleName) {
    Assert.notEmpty(moduleName);
    return modules.containsKey(moduleName);
  }

  private BeeModule getModule(String moduleName) {
    Assert.state(hasModule(moduleName));
    return modules.get(moduleName);
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    Collection<String> mods = Sets.newHashSet(CommonsConstants.COMMONS_MODULE);
    String moduleList = Config.getProperty(PROPERTY_MODULES);

    if (!BeeUtils.isEmpty(moduleList)) {
      for (String mod : SPLITTER.split(moduleList)) {
        mods.add(mod);
      }
    }
    for (String mod : mods) {
      try {
        BeeModule module =
            (BeeModule) InitialContext.doLookup("java:global/Bee/" + mod + MODULE_BEAN_PREFIX);
        String moduleName = module.getName();

        if (BeeUtils.isEmpty(moduleName)) {
          LogUtils.severe(logger, "Module", BeeUtils.bracket(mod), "does not have name");

        } else if (hasModule(moduleName)) {
          LogUtils.severe(logger, "Dublicate module name:", BeeUtils.bracket(moduleName));

        } else {
          modules.put(moduleName, module);
          LogUtils.info(logger, "Registered module:", BeeUtils.bracket(moduleName));
        }
      } catch (NamingException ex) {
        LogUtils.severe(logger, "Module not found:", BeeUtils.bracket(mod));

      } catch (ClassCastException ex) {
        LogUtils.severe(logger, "Not a module:", BeeUtils.bracket(mod));
      }
    }
    boolean dependencyError = true;

    while (dependencyError) {
      dependencyError = false;

      for (String mod : getModules()) {
        moduleList = getModule(mod).dependsOn();

        if (!BeeUtils.isEmpty(moduleList)) {
          for (String depends : SPLITTER.split(moduleList)) {
            if (!hasModule(depends)) {
              LogUtils.severe(logger, "Module dependency error:", "Module", BeeUtils.bracket(mod),
                  "depends on nonexistent module", BeeUtils.bracket(depends));
              modules.remove(mod);
              dependencyError = true;
              break;
            }
          }
        }
      }
    }
    if (modules.isEmpty()) {
      LogUtils.warning(logger, "No modules registered");
    }
  }
}
