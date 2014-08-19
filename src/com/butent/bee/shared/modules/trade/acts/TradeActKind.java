package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeActKind implements HasLocalizedCaption {
  SALE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindSale();
    }

    @Override
    public Filter getFilter() {
      return Filter.or(super.getFilter(), SUPPLEMENT.getFilter());
    }
  },
  SUPPLEMENT {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindSupplement();
    }

    @Override
    public String getGridSupplierKey() {
      return null;
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

  public Filter getFilter() {
    return Filter.isEqual(TradeActConstants.COL_TA_KIND, IntegerValue.of(this));
  }

  public String getGridSupplierKey() {
    return TradeActConstants.GRID_TRADE_ACTS + BeeConst.STRING_UNDER + name().toLowerCase();
  }
}
