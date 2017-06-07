package com.butent.bee.shared.modules.trade.acts;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

import java.util.EnumSet;

public enum TradeActKind implements HasLocalizedCaption {
  /* 0 */
  SALE(new String[] {COL_TA_INPUT_DRIVER, COL_TA_INPUT_VEHICLE, COL_TA_OBJECT, COL_TA_STATUS},
      Option.ALTER_TO,
      Option.AUTO_NUMBER, Option.BUILD_INVOICES,
      Option.ENABLE_COPY,
      Option.ENABLE_RETURN, Option.ENABLE_SUPPLEMENT, Option.HAS_SERVICES, Option.SAVE_AS_TEMPLATE,
      Option.SHOW_STOCK) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taKindSale();
    }

    @Override
    public Filter getFilter() {
      return Filter.or(Lists.newArrayList(super.getFilter(), SUPPLEMENT.getFilter(),
          RETURN.getFilter(), CONTINUOUS.getFilter()));
    }
  },

  /* 1 */
  SUPPLEMENT(new String[] {COL_TA_INPUT_DRIVER, COL_TA_INPUT_VEHICLE, COL_TA_OBJECT, COL_TA_STATUS},
      Option.BUILD_INVOICES, Option.ENABLE_RETURN, Option.HAS_SERVICES,
      Option.SHOW_STOCK, Option.HAS_PARENT_ACT) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taKindSupplement();
    }

    @Override
    public String getGridSupplierKey() {
      return null;
    }
  },

  /* 2 */
  RETURN(new String[] {COL_TA_INPUT_DRIVER, COL_TA_INPUT_VEHICLE,
    COL_TA_OBJECT, COL_TA_STATUS, COL_TA_DATE},
      Option.AUTO_NUMBER, Option.HAS_PARENT_ACT) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taKindReturn();
    }

    @Override
    public String getGridSupplierKey() {
      return null;
    }
  },

  /* 3 */
  TENDER(null, Option.ALTER_TO, Option.ALTER_FROM, Option.ENABLE_COPY,
      Option.HAS_SERVICES,
      Option.SAVE_AS_TEMPLATE, Option.SHOW_STOCK) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taKindTender();
    }
  },

  /* 4 */
  PURCHASE(null, Option.ALTER_TO, Option.AUTO_NUMBER, Option.ENABLE_COPY,
      Option.SAVE_AS_TEMPLATE) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taKindPurchase();
    }
  },

  /* 5 */
  WRITE_OFF(null, Option.ALTER_TO, Option.AUTO_NUMBER, Option.SHOW_STOCK) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taKindWriteOff();
    }

    @Override
    public String getStyleSuffix() {
      return "write-off";
    }
  },

  /* 6 */
  RESERVE(null, Option.ALTER_TO, Option.ALTER_FROM, Option.HAS_SERVICES,
      Option.SHOW_STOCK) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.taKindReserve();
    }
  },

  /* 7 */
  CONTINUOUS(null, Option.AUTO_NUMBER, Option.HAS_SERVICES, Option.SHOW_STOCK,
      Option.ENABLE_RETURN, Option.BUILD_INVOICES) {
    @Override
    public  String getCaption(Dictionary constants) {
      return constants.taKindContinuous();
    }

    @Override
    public boolean enableMultiReturn(TradeActKind other) {
      return super.enableMultiReturn(other);
    }
  };

  private enum Option {
    ALTER_FROM,
    ALTER_TO,
    AUTO_NUMBER,
    BUILD_INVOICES,
    ENABLE_COPY,
    ENABLE_RETURN,
    ENABLE_SUPPLEMENT,
    HAS_SERVICES,
    SAVE_AS_TEMPLATE,
    SHOW_STOCK,
    HAS_PARENT_ACT
  }

  public static Filter getFilterForInvoiceBuilder() {
    CompoundFilter filter = Filter.or();

    for (TradeActKind kind : values()) {
      if (kind.enableInvoices()) {
        filter.add(Filter.equals(TradeActConstants.COL_TA_KIND, kind));
      }
    }
    return filter;
  }

  private final EnumSet<Option> options;
  private String[] reqFields;

  TradeActKind(EnumSet<Option> options) {
    this.options = options;
  }

  TradeActKind(String[] reqFields, Option first, Option... rest) {
    this.reqFields = reqFields;
    if (rest == null) {
      this.options = EnumSet.of(first);
    } else {
      this.options = EnumSet.of(first, rest);
    }
  }

  public String[] getReqFields() {
    return reqFields;
  }

  public boolean autoNumber() {
    return options.contains(Option.AUTO_NUMBER);
  }

  public boolean enableAlter() {
    return options.contains(Option.ALTER_FROM);
  }

  public boolean enableCopy() {
    return options.contains(Option.ENABLE_COPY);
  }

  public boolean enableInvoices() {
    return options.contains(Option.BUILD_INVOICES);
  }

  public boolean enableReturn() {
    return options.contains(Option.ENABLE_RETURN);
  }

  public boolean enableMultiReturn(TradeActKind other) {
    return enableReturn() && other != null && other.enableReturn();
  }

  public boolean enableServices() {
    return options.contains(Option.HAS_SERVICES);
  }

  public boolean enableSupplement() {
    return options.contains(Option.ENABLE_SUPPLEMENT);
  }

  public boolean enableTemplate() {
    return options.contains(Option.SAVE_AS_TEMPLATE);
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

  public boolean isAlterTarget() {
    return options.contains(Option.ALTER_TO);
  }

  public boolean showStock() {
    return options.contains(Option.SHOW_STOCK);
  }

}
