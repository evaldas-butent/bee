package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeActTimeUnit implements HasLocalizedCaption {
  DAY {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taTimeUnitDay();
    }
  },
  MONTH {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taTimeUnitMonth();
    }
  };
}
