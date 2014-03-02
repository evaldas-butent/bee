package com.butent.bee.shared.rights;

import com.google.common.collect.Sets;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.EnumSet;
import java.util.Set;

public enum Module implements HasLocalizedCaption {

  CLASSIFIERS(EnumSet.of(SubModule.CONTACTS)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.classifiers();
    }
  },
  CALENDAR(EnumSet.noneOf(SubModule.class)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.calendar();
    }
  },
  DOCUMENTS(EnumSet.noneOf(SubModule.class)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.documents();
    }
  },
  TASKS(EnumSet.noneOf(SubModule.class)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.tasks();
    }
  },
  DISCUSSIONS(EnumSet.noneOf(SubModule.class)) {
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
  TRADE(EnumSet.noneOf(SubModule.class)) {
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
  ADMINISTRATION(EnumSet.noneOf(SubModule.class)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.administration();
    }
  };

  static final Set<ModuleAndSub> ENABLED_MODULES = Sets.newHashSet();
  
  public static String getEnabledModulesAsString() {
    return BeeUtils.joinItems(ENABLED_MODULES);
  }

  public static boolean isEnabled(String input) {
    if (BeeUtils.isEmpty(input)) {
      return true;
    }

    ModuleAndSub ms = ModuleAndSub.parse(input);
    if (ms == null) {
      return false;
    } else {
      return ENABLED_MODULES.contains(ms);
    }
  }

  public static void setEnabledModules(String moduleList) {
    if (!ENABLED_MODULES.isEmpty()) {
      ENABLED_MODULES.clear();
    }

    if (BeeUtils.isEmpty(moduleList) || BeeUtils.same(moduleList, BeeConst.ALL)) {
      for (Module module : Module.values()) {
        ENABLED_MODULES.add(ModuleAndSub.of(module));

        if (!BeeUtils.isEmpty(module.getSubModules())) {
          for (SubModule subModule : module.getSubModules()) {
            ENABLED_MODULES.add(ModuleAndSub.of(module, subModule));
          }
        }
      }

    } else {
      Set<String> modules = NameUtils.toSet(moduleList);

      for (String input : modules) {
        ModuleAndSub ms = ModuleAndSub.parse(input);

        if (ms != null) {
          if (ms.getSubModule() != null) {
            ModuleAndSub parent = ModuleAndSub.of(ms.getModule());
            if (!ENABLED_MODULES.contains(parent)) {
              ENABLED_MODULES.add(parent);
            }
          }

          ENABLED_MODULES.add(ms);
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

  public EnumSet<SubModule> getSubModules() {
    return subModules;
  }
  
  public boolean isEnabled() {
    return ENABLED_MODULES.contains(ModuleAndSub.of(this));
  }
}
