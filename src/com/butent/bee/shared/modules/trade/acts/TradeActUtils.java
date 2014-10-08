package com.butent.bee.shared.modules.trade.acts;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.i18n.Localized;
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

  public static String getLabel(String name, boolean plural) {
    Assert.notEmpty(name);

    switch (name) {
      case COL_TA_COMPANY:
        return plural ? Localized.getConstants().clients() : Localized.getConstants().client();

      case COL_TA_OBJECT:
        return plural ? Localized.getConstants().objects() : Localized.getConstants().object();

      case COL_TA_OPERATION:
        return plural ? Localized.getConstants().trdOperationsShort()
            : Localized.getConstants().trdOperation();

      case COL_TA_STATUS:
        return plural ? Localized.getConstants().trdStatuses() : Localized.getConstants().status();

      case COL_TA_SERIES:
        return plural ? Localized.getConstants().trdSeriesPlural()
            : Localized.getConstants().trdSeries();

      case COL_TA_MANAGER:
        return plural ? Localized.getConstants().managers() : Localized.getConstants().manager();

      case COL_WAREHOUSE:
        return plural ? Localized.getConstants().warehouses()
            : Localized.getConstants().warehouse();

      case COL_CATEGORY:
        return plural ? Localized.getConstants().categories() : Localized.getConstants().category();

      case COL_TA_ITEM:
        return plural ? Localized.getConstants().goods() : Localized.getConstants().item();

      default:
        return null;
    }
  }

  private TradeActUtils() {
  }
}
