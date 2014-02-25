package com.butent.bee.shared.rights;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;

public enum SubModule implements HasLocalizedCaption {
  ADMINISTRATION {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return null;
    }
  },
  CONTACTS {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.contacts();
    }
  };

  @Override
  public String getCaption() {
    return getCaption(Localized.getConstants());
  }

  public String getName() {
    return BeeUtils.proper(name());
  }
}
