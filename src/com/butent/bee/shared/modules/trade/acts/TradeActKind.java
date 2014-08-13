package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeActKind implements HasLocalizedCaption {
  SALE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindSale();
    }
  },
  SUPPLEMENT {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindSupplement();
    }
  },
  RETURN {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindReturn();
    }
  },
  TENDER {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindTender();
    }
  },
  PURCHASE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindPurchase();
    }
  },
  WRITE_OFF {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindWriteOff();
    }
  },
  RESERVE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindReserve();
    }
  };

  @Override
  public String getCaption() {
    return getCaption(Localized.getConstants());
  }
}
