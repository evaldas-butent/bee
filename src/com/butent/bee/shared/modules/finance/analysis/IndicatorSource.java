package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.modules.finance.FinanceConstants;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum IndicatorSource implements HasLocalizedCaption {
  AMOUNT {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.amount();
    }

    @Override
    public String getSourceColumn() {
      return FinanceConstants.COL_FIN_AMOUNT;
    }
  },

  QUANTITY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.quantity();
    }

    @Override
    public String getSourceColumn() {
      return FinanceConstants.COL_FIN_QUANTITY;
    }
  };

  public static final IndicatorSource DEFAULT = AMOUNT;

  public abstract String getSourceColumn();
}
