package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeActKind implements HasLocalizedCaption {
  SALE(true, true, true, true, true, true) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindSale();
    }

    @Override
    public Filter getFilter() {
      return Filter.or(super.getFilter(), SUPPLEMENT.getFilter(), RETURN.getFilter());
    }
  },
  SUPPLEMENT(false, false, true, true, false, true) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindSupplement();
    }

    @Override
    public String getGridSupplierKey() {
      return null;
    }
  },
  RETURN(false, false, false, false, false, false) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindReturn();
    }

    @Override
    public String getGridSupplierKey() {
      return null;
    }
  },
  TENDER(true, true, true, true, false, false) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindTender();
    }
  },
  PURCHASE(true, true, false, false, true, false) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindPurchase();
    }
  },
  WRITE_OFF(false, false, false, true, true, false) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindWriteOff();
    }

    @Override
    public String getStyleSuffix() {
      return "write-off";
    }
  },
  RESERVE(false, false, false, true, false, false) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.taKindReserve();
    }
  };

  public static Filter getFilterForInvoiceBuilder() {
    CompoundFilter filter = Filter.or();

    for (TradeActKind kind : values()) {
      if (kind.enableInvoices()) {
        filter.add(kind.getFilter());
      }
    }
    return filter;
  }

  private final boolean copy;
  private final boolean template;
  private final boolean services;
  private final boolean showStock;
  private final boolean number;
  private final boolean invoices;

  private TradeActKind(boolean copy, boolean template, boolean services, boolean showStock,
      boolean number, boolean invoices) {

    this.copy = copy;
    this.template = template;
    this.services = services;
    this.showStock = showStock;
    this.number = number;
    this.invoices = invoices;
  }

  public boolean autoNumber() {
    return number;
  }

  public boolean enableCopy() {
    return copy;
  }

  public boolean enableInvoices() {
    return invoices;
  }

  public boolean enableReturn() {
    return this == SALE;
  }

  public boolean enableServices() {
    return services;
  }

  public boolean enableSupplement() {
    return this == SALE;
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
