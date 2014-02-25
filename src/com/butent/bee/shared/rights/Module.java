package com.butent.bee.shared.rights;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;

public enum Module implements HasLocalizedCaption {
  CLASSIFIERS(EnumSet.of(SubModule.CONTACTS)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.classifiers();
    }
  },
  CALENDAR(EnumSet.of(SubModule.ADMINISTRATION)) {
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
  MAIL(null) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.mail();
    }
  },
  E_COMMERCE(null) {
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
  TRANSPORT(null) {
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

  private EnumSet<SubModule> subModules;

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
}
