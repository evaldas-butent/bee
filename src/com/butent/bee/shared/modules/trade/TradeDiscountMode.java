package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeDiscountMode implements HasLocalizedCaption {
  FROM_AMOUNT {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.discountFromAmount();
    }
  },

  FROM_PRICE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.discountFromPrice();
    }
  }
}
