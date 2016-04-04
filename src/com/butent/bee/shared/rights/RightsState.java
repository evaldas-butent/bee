package com.butent.bee.shared.rights;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum RightsState implements HasLocalizedCaption {
  VIEW {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateView();
    }
  },
  CREATE {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateCreate();
    }
  },
  EDIT {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateEdit();
    }
  },
  DELETE {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateDelete();
    }
  },
  MERGE {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateMerge();
    }

    @Override
    public boolean isChecked() {
      return false;
    }
  },
  REQUIRED {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateRequired();
    }

    @Override
    public boolean isChecked() {
      return false;
    }
  };

  public boolean isChecked() {
    return true;
  }
}
