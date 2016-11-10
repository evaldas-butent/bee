package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum IndicatorBalance implements HasLocalizedCaption {
  TURNOVER {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finTurnover();
    }
  },

  OPENING_BALANCE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finOpeningBalance();
    }
  },

  CLOSING_BALANCE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finClosingBalance();
    }
  }
}
