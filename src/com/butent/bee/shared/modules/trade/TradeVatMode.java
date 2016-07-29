package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeVatMode implements HasLocalizedCaption {
  PLUS {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.vatModePlus();
    }
  },

  INCLUSIVE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.vatModeInclusive();
    }
  }
}
