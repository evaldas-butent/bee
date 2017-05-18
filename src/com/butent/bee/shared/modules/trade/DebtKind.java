package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum DebtKind implements HasLocalizedCaption {
  PAYABLE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.trdAccountsPayable();
    }

    @Override
    public String getPayerLabel(Dictionary dictionary) {
      return dictionary.supplier();
    }

    @Override
    public String tradeDocumentCompanyColumn() {
      return TradeConstants.COL_TRADE_SUPPLIER;
    }
  },

  RECEIVABLE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.trdAccountsReceivable();
    }

    @Override
    public String getPayerLabel(Dictionary dictionary) {
      return dictionary.payer();
    }

    @Override
    public String tradeDocumentCompanyColumn() {
      return TradeConstants.COL_TRADE_CUSTOMER;
    }
  };

  public abstract String getPayerLabel(Dictionary dictionary);

  public abstract String tradeDocumentCompanyColumn();
}
