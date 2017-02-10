package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.modules.finance.FinanceConstants;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum IndicatorKind implements HasLocalizedCaption {
  PRIMARY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finIndicatorPrimary();
    }

    @Override
    public TurnoverOrBalance getDefaultTurnoverOrBalance() {
      return TurnoverOrBalance.TURNOVER;
    }

    @Override
    public IndicatorSource getDefaultSource() {
      return IndicatorSource.AMOUNT;
    }

    @Override
    public String getEditForm() {
      return FinanceConstants.FORM_FINANCIAL_INDICATOR_PRIMARY;
    }
  },

  SECONDARY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finIndicatorSecondary();
    }

    @Override
    public TurnoverOrBalance getDefaultTurnoverOrBalance() {
      return null;
    }

    @Override
    public IndicatorSource getDefaultSource() {
      return null;
    }

    @Override
    public String getEditForm() {
      return FinanceConstants.FORM_FINANCIAL_INDICATOR_SECONDARY;
    }
  };

  public abstract TurnoverOrBalance getDefaultTurnoverOrBalance();

  public abstract IndicatorSource getDefaultSource();

  public abstract String getEditForm();
}
