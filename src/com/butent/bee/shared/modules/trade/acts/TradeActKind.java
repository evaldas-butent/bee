package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeActKind implements HasLocalizedCaption {
  SALE(true, true, true, true) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindSale();
    }

    @Override
    public Filter getFilter() {
      return Filter.or(super.getFilter(), SUPPLEMENT.getFilter(), RETURN.getFilter());
    }
  },
  SUPPLEMENT(false, false, true, true) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindSupplement();
    }

    @Override
    public String getGridSupplierKey() {
      return null;
    }
  },
  RETURN(false, false, false, false) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindReturn();
    }

    @Override
    public String getGridSupplierKey() {
      return null;
    }
  },
  TENDER(true, true, true, true) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindTender();
    }
  },
  PURCHASE(true, true, false, false) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindPurchase();
    }
  },
  WRITE_OFF(false, false, false, true) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindWriteOff();
    }

    @Override
    public String getStyleSuffix() {
      return "write-off";
    }
  },
  RESERVE(false, false, false, true) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindReserve();
    }
  };

  private final boolean copy;
  private final boolean template;
  private final boolean services;
  private final boolean showStock;

  private TradeActKind(boolean copy, boolean template, boolean services, boolean showStock) {
    this.copy = copy;
    this.template = template;
    this.services = services;
    this.showStock = showStock;
  }

  public boolean enableCopy() {
    return copy;
  }

  public boolean enableServices() {
    return services;
  }

  public boolean enableTemplate() {
    return template;
  }

  @Override
  public String getCaption() {
    return getCaption(Localized.getConstants());
  }

  public Filter getFilter() {
    return Filter.equals(TradeActConstants.COL_TA_KIND, this);
  }

  public String getGridSupplierKey() {
    return TradeActConstants.GRID_TRADE_ACTS + BeeConst.STRING_UNDER + name().toLowerCase();
  }

  public String getStyleSuffix() {
    return name().toLowerCase();
  }

  public boolean showStock() {
    return showStock;
  }
}
