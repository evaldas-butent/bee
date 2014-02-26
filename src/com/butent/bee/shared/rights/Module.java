package com.butent.bee.shared.rights;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum Module implements HasLocalizedCaption {

  CLASSIFIERS(EnumSet.of(SubModule.CONTACTS)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.classifiers();
    }
  },
  CALENDAR(null) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.calendar();
    }
  },
  DOCUMENTS(null) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.documents();
    }
  },
  TASKS(null) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.tasks();
    }
  },
  DISCUSSIONS(null) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.discussions();
    }
  },
  MAIL(EnumSet.of(SubModule.ADMINISTRATION)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.mail();
    }
  },
  ECOMMERCE(EnumSet.of(SubModule.ADMINISTRATION)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.ecModule();
    }

    @Override
    public String getName() {
      return "Ec";
    }
  },
  TRADE(null) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trade();
    }
  },
  TRANSPORT(EnumSet.of(SubModule.ADMINISTRATION)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.transport();
    }
  },
  ADMINISTRATION(null) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.administration();
    }
  };

  private static final Set<String> ENABLED_MODULES = Sets.newHashSet();

  public static Set<String> getEnabledModules() {
    return ENABLED_MODULES;
  }

  public static boolean isEnabled(String module) {
    if (!BeeUtils.isEmpty(module)) {
      String mod = null;

      for (String part : RightsUtils.SPLITTER.split(module)) {
        mod = RightsUtils.JOINER.join(mod, part);

        if (!getEnabledModules().contains(mod)) {
          return false;
        }
      }
    }
    return true;
  }

  public static void setEnabledModules(String moduleList) {
    Map<String, String> modules = Maps.newLinkedHashMap();

    for (Module module : Module.values()) {
      String name = module.getName();
      modules.put(BeeUtils.normalize(name), name);

      if (!BeeUtils.isEmpty(module.getSubModules())) {
        for (SubModule subModule : module.getSubModules()) {
          name = module.getName(subModule);
          modules.put(BeeUtils.normalize(name), name);
        }
      }
    }
    getEnabledModules().clear();

    if (BeeUtils.isEmpty(moduleList) || BeeUtils.same(moduleList, BeeConst.ALL)) {
      getEnabledModules().addAll(modules.values());
    } else {
      for (String mod : Splitter.on(BeeConst.DEFAULT_LIST_SEPARATOR).omitEmptyStrings()
          .trimResults().split(moduleList)) {
        String name = modules.get(BeeUtils.normalize(mod));

        if (!BeeUtils.isEmpty(name)) {
          getEnabledModules().add(name);
        }
      }
    }
  }

  private final EnumSet<SubModule> subModules;

  private Module(EnumSet<SubModule> subModules) {
    this.subModules = subModules;
  }

  @Override
  public String getCaption() {
    return getCaption(Localized.getConstants());
  }

  public String getName() {
    return BeeUtils.proper(name());
  }

  public String getName(SubModule subModule) {
    Assert.notNull(subModule);
    return RightsUtils.JOINER.join(getName(), subModule.getName());
  }

  public EnumSet<SubModule> getSubModules() {
    return subModules;
  }
}
