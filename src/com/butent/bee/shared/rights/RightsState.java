package com.butent.bee.shared.rights;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum RightsState implements HasLocalizedCaption {
  VIEW(true) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateView();
    }
  },
  CREATE(true) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateCreate();
    }
  },
  EDIT(true) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateEdit();
    }
  },
  DELETE(true) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateDelete();
    }
  },
  MERGE(false) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.rightStateMerge();
    }
  },
  REQUIRED(true) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.notRequired();
    }
  };

  private final boolean checked;

  RightsState(boolean checked) {
    this.checked = checked;
  }

  public boolean isChecked() {
    return checked;
  }
}
