package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum IndicatorKind implements HasLocalizedCaption {
  PRIMARY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finIndicatorPrimary();
    }

    @Override
    public IndicatorBalance getDefaultBalance() {
      return IndicatorBalance.TURNOVER;
    }

    @Override
    public IndicatorSource getDefaultSource() {
      return IndicatorSource.AMOUNT;
    }
  },

  SECONDARY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finIndicatorSecondary();
    }

    @Override
    public IndicatorBalance getDefaultBalance() {
      return null;
    }

    @Override
    public IndicatorSource getDefaultSource() {
      return null;
    }
  };

  public abstract IndicatorBalance getDefaultBalance();

  public abstract IndicatorSource getDefaultSource();
}
