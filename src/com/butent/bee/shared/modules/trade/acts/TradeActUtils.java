package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.utils.BeeUtils;

public final class TradeActUtils {

  public static Double calculateServicePrice(Double itemTotal, Double tariff, Integer scale) {
    Double price = BeeUtils.percent(itemTotal, tariff);

    if (BeeUtils.nonZero(price) && BeeUtils.isNonNegative(scale)) {
      return BeeUtils.round(price, scale);
    } else {
      return price;
    }
  }

  private TradeActUtils() {
  }
}
