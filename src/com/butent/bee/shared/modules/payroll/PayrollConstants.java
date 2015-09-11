package com.butent.bee.shared.modules.payroll;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.EnumUtils;

public final class PayrollConstants {

  public enum ObjectStatus implements HasLocalizedCaption {
    INACTIVE {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.objectStatusInactive();
      }
    },
    ACTIVE {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.objectStatusActive();
      }
    };

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }
  }

  public static void register() {
    EnumUtils.register(ObjectStatus.class);
  }

  private PayrollConstants() {
  }
}
