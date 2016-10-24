package com.butent.bee.client.modules.trade;

import com.google.gwt.i18n.client.NumberFormat;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import com.butent.bee.shared.utils.BeeUtils;

public final class CustomTradeUtils {

  private static final String COLUMN_SUFFIX = "Localized";
  private static final String TOTAL = "Total";

  private CustomTradeUtils() {
  }

  public static String format(String fld, double amountSum, double rateAmount) {
    return format(fld, amountSum, rateAmount, 0, 0, 0, 0, 0, 0);
  }

  public static String format(String fld, double amountSumTotal, double vatTotal, double total,
      double rateAmountTotal, double rateVatTotal, double rateTotal) {
    return format(fld, 0, 0, amountSumTotal, vatTotal, total, rateAmountTotal, rateVatTotal,
        rateTotal);
  }

  public static String format(String fld, double amountSum, double rateAmount,
      double amountSumTotal, double vatTotal, double total, double rateAmountTotal,
      double rateVatTotal, double rateTotal) {
    NumberFormat formatter = NumberFormat.getFormat("#,##0.00");
    Double number = null;
    if (BeeUtils.same(fld, COL_TRADE_AMOUNT + COLUMN_SUFFIX)) {
      number = amountSum;

    } else if (BeeUtils.same(fld, "RateAmount" + COLUMN_SUFFIX)) {
      number = rateAmount;

    } else if (BeeUtils.same(fld, COL_TRADE_AMOUNT + TOTAL + COLUMN_SUFFIX)) {
      number = amountSumTotal;

    } else if (BeeUtils.same(fld, COL_TRADE_VAT + TOTAL + COLUMN_SUFFIX)) {
      number = vatTotal;

    } else if (BeeUtils.same(fld, TOTAL + COLUMN_SUFFIX)) {
      number = total;

    } else if (BeeUtils.same(fld, "RateAmount" + TOTAL + COLUMN_SUFFIX)) {
      number = rateAmountTotal;

    } else if (BeeUtils.same(fld, "RateVat" + TOTAL + COLUMN_SUFFIX)) {
      number = rateVatTotal;

    } else if (BeeUtils.same(fld, "Rate" + TOTAL + COLUMN_SUFFIX)) {
      number = rateTotal;
    }
    if (number != null) {
      return formatter.format(number).replaceAll("\\s", ".");
    }
    return null;
  }
}

