package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeCostBasis implements HasLocalizedCaption {
  AMOUNT {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.amount();
    }
  },

  QUANTITY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.quantity();
    }
  },

  WEIGHT {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.weight();
    }
  }
}
