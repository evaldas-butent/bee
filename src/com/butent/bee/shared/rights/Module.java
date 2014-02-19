package com.butent.bee.shared.rights;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum Module implements HasLocalizedCaption {
  CLASSIFIERS {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.classifiers();
    }
  },
  CONTACTS {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.contacts();
    }
  },
  CALENDAR {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.calendar();
    }
  },
  DOCUMENTS {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.documents();
    }
  },
  TASKS {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.tasks();
    }
  },
  DISCUSSIONS {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.discussions();
    }
  },
  MAIL {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.mail();
    }
  },
  E_COMMERCE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.ecModule();
    }
  },
  TRADE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trade();
    }
  },
  TRANSPORT {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.transport();
    }
  },
  ADMINISTRATION {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.administration();
    }
  };
  
  @Override
  public String getCaption() {
    return getCaption(Localized.getConstants());
  }
}
