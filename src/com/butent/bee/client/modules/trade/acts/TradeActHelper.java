package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.i18n.client.NumberFormat;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class TradeActHelper {

  private static final BeeLogger logger = LogUtils.getLogger(TradeActHelper.class);

  private static NumberFormat quantityFormat;
  private static NumberFormat discountPercentFormat;
  private static NumberFormat priceFormat;
  private static NumberFormat amountFormat;

  private TradeActHelper() {
  }

  static String getLabel(String name) {
    switch (name) {
      case COL_TRADE_ACT:
        return Localized.getConstants().tradeAct();

      case COL_TA_NAME:
        return Localized.getConstants().tradeActName();

      case COL_TA_DATE:
        return Localized.getConstants().taDate();

      case COL_TA_SERIES:
      case COL_SERIES_NAME:
        return Localized.getConstants().trdSeries();

      case COL_TA_NUMBER:
        return Localized.getConstants().number();

      case COL_TA_OPERATION:
      case COL_OPERATION_NAME:
        return Localized.getConstants().trdOperation();

      case COL_TA_STATUS:
      case COL_STATUS_NAME:
        return Localized.getConstants().status();

      case COL_TA_COMPANY:
      case ALS_COMPANY_NAME:
        return Localized.getConstants().client();

      case COL_TA_OBJECT:
      case COL_COMPANY_OBJECT_NAME:
        return Localized.getConstants().object();

      case COL_TA_MANAGER:
        return Localized.getConstants().manager();

      case COL_FIRST_NAME:
        return Localized.getConstants().firstName();
      case COL_LAST_NAME:
        return Localized.getConstants().lastName();

      case COL_WAREHOUSE:
      case ALS_WAREHOUSE_CODE:
        return Localized.getConstants().warehouse();

      case COL_ITEM_TYPE:
      case ALS_ITEM_TYPE_NAME:
        return Localized.getConstants().type();

      case COL_ITEM_GROUP:
      case ALS_ITEM_GROUP_NAME:
        return Localized.getConstants().group();

      case COL_TA_ITEM:
        return Localized.getConstants().item();

      case ALS_ITEM_NAME:
        return Localized.getConstants().itemName();
      case COL_ITEM_ARTICLE:
        return Localized.getConstants().article();

      case COL_UNIT:
      case ALS_UNIT_NAME:
        return Localized.getConstants().unitShort();

      case COL_TRADE_ITEM_QUANTITY:
        return Localized.getConstants().quantity();
      case ALS_RETURNED_QTY:
        return Localized.getConstants().taQuantityReturned();
      case ALS_REMAINING_QTY:
        return Localized.getConstants().taQuantityRemained();

      case COL_TRADE_ITEM_PRICE:
        return Localized.getConstants().price();

      case COL_TRADE_DISCOUNT:
        return Localized.getConstants().discountPercent();
      case ALS_DISCOUNT_AMOUNT:
        return Localized.getConstants().discount();

      case ALS_BASE_AMOUNT:
        return Localized.getConstants().amount();
      case ALS_TOTAL_AMOUNT:
        return Localized.getConstants().total();

      default:
        logger.warning(NameUtils.getClassName(TradeActHelper.class), name, "label not defined");
        return null;
    }
  }

  static String getLabel(String name, boolean plural) {
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
        logger.warning(NameUtils.getClassName(TradeActHelper.class), name, plural,
            "label not defined");
        return null;
    }
  }

  static NumberFormat getNumberFormat(String name) {
    switch (name) {
      case COL_TRADE_ACT:
        return Format.getDefaultLongFormat();

      case COL_TRADE_ITEM_QUANTITY:
      case ALS_RETURNED_QTY:
      case ALS_REMAINING_QTY:
        return getQuantityFormat();

      case COL_TRADE_ITEM_PRICE:
        return getPriceFormat();

      case COL_TRADE_DISCOUNT:
        return getDiscountPercentFormat();

      case ALS_BASE_AMOUNT:
      case ALS_DISCOUNT_AMOUNT:
      case ALS_TOTAL_AMOUNT:
        return getAmountFormat();

      default:
        logger.warning(NameUtils.getClassName(TradeActHelper.class), name, "format not defined");
        return null;
    }
  }

  static NumberFormat getQuantityFormat() {
    if (quantityFormat == null) {
      Integer scale = Data.getColumnScale(VIEW_TRADE_ACT_ITEMS, COL_TRADE_ITEM_QUANTITY);
      quantityFormat = Format.getDecimalFormat(0, BeeUtils.unbox(scale));
    }
    return quantityFormat;
  }

  static NumberFormat getPriceFormat() {
    if (priceFormat == null) {
      priceFormat = Format.getDefaultMoneyFormat();
    }
    return priceFormat;
  }

  static NumberFormat getDiscountPercentFormat() {
    if (discountPercentFormat == null) {
      Integer scale = Data.getColumnScale(VIEW_TRADE_ACT_ITEMS, COL_TRADE_DISCOUNT);
      discountPercentFormat = Format.getDecimalFormat(0, BeeUtils.unbox(scale));
    }
    return discountPercentFormat;
  }

  static NumberFormat getAmountFormat() {
    if (amountFormat == null) {
      amountFormat = Format.getDefaultMoneyFormat();
    }
    return amountFormat;
  }

  static ValueType getType(Collection<String> viewNames, String colName) {
    switch (colName) {
      case ALS_RETURNED_QTY:
      case ALS_REMAINING_QTY:

      case ALS_BASE_AMOUNT:
      case ALS_DISCOUNT_AMOUNT:
      case ALS_TOTAL_AMOUNT:
        return ValueType.NUMBER;

      case ALS_WAREHOUSE_CODE:
      case ALS_ITEM_TYPE_NAME:
      case ALS_ITEM_GROUP_NAME:
        return ValueType.TEXT;
    }

    for (String viewName : viewNames) {
      ValueType type = Data.getColumnType(viewName, colName);
      if (type != null) {
        return type;
      }
    }

    logger.warning(NameUtils.getClassName(TradeActHelper.class), viewNames, colName,
        "type not defined");
    return null;
  }

  static List<ValueType> getTypes(Collection<String> viewNames, SimpleRowSet rowSet) {
    List<ValueType> types = new ArrayList<>();

    for (String colName : rowSet.getColumnNames()) {
      types.add(getType(viewNames, colName));
    }

    return types;
  }
}
