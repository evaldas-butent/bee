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
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class TradeActHelper {

  private static final BeeLogger logger = LogUtils.getLogger(TradeActHelper.class);

  private static NumberFormat amountFormat;
  private static NumberFormat discountPercentFormat;
  private static NumberFormat priceFormat;
  private static NumberFormat quantityFormat;
  private static NumberFormat weightFormat;

  private TradeActHelper() {
  }

  static NumberFormat getDecimalFormat(String viewName, String colName) {
    Integer scale = Data.getColumnScale(viewName, colName);
    return Format.getDecimalFormat(0, BeeUtils.unbox(scale));
  }

  static String getLabel(String name) {
    switch (name) {
      case COL_TRADE_ACT:
        return Localized.dictionary().tradeAct();

      case COL_TA_NAME:
      case COL_TRADE_ACT_NAME:
        return Localized.dictionary().tradeActName();

      case COL_TA_DATE:
        return Localized.dictionary().taDate();
      case COL_TA_UNTIL:
        return Localized.dictionary().taUntil();

      case COL_TA_SERIES:
      case COL_SERIES_NAME:
        return Localized.dictionary().trdSeries();

      case COL_TA_NUMBER:
        return Localized.dictionary().number();

      case COL_TA_OPERATION:
      case COL_OPERATION_NAME:
        return Localized.dictionary().trdOperation();

      case COL_TA_STATUS:
      case COL_STATUS_NAME:
        return Localized.dictionary().status();

      case COL_TA_COMPANY:
      case ALS_COMPANY_NAME:
        return Localized.dictionary().client();

      case COL_TA_OBJECT:
      case COL_COMPANY_OBJECT_NAME:
        return Localized.dictionary().object();

      case COL_TA_MANAGER:
        return Localized.dictionary().manager();

      case COL_FIRST_NAME:
        return Localized.dictionary().firstName();
      case COL_LAST_NAME:
        return Localized.dictionary().lastName();

      case COL_WAREHOUSE:
      case ALS_WAREHOUSE_CODE:
        return Localized.dictionary().warehouse();

      case COL_ITEM_TYPE:
      case ALS_ITEM_TYPE_NAME:
        return Localized.dictionary().type();

      case COL_ITEM_GROUP:
      case ALS_ITEM_GROUP_NAME:
        return Localized.dictionary().group();

      case COL_TA_ITEM:
        return Localized.dictionary().item();

      case ALS_ITEM_NAME:
        return Localized.dictionary().itemName();
      case COL_ITEM_ARTICLE:
        return Localized.dictionary().article();
      case COL_ITEM_WEIGHT:
        return Localized.dictionary().weight();

      case ALS_SUPPLIER_NAME:
        return Localized.dictionary().supplier();
      case COL_COST_AMOUNT:
        return "Sav.suma";

      case COL_UNIT:
      case ALS_UNIT_NAME:
        return Localized.dictionary().unitShort();
      case COL_TIME_UNIT:
        return Localized.dictionary().taTimeUnit();

      case COL_TRADE_ITEM_QUANTITY:
        return Localized.dictionary().quantity();
      case TradeActConstants.ALS_RETURNED_QTY:
        return Localized.dictionary().taQuantityReturned();
      case ALS_REMAINING_QTY:
        return Localized.dictionary().taQuantityRemained();

      case COL_TRADE_ITEM_PRICE:
        return Localized.dictionary().price();

      case ALS_BASE_AMOUNT:
        return Localized.dictionary().amount();

      case COL_TRADE_DISCOUNT:
        return Localized.dictionary().discountPercent();
      case ALS_DISCOUNT_AMOUNT:
        return Localized.dictionary().discount();

      case ALS_WITHOUT_VAT:
        return Localized.dictionary().trdAmountWoVat();
      case ALS_VAT_AMOUNT:
        return Localized.dictionary().vatAmount();

      case ALS_TOTAL_AMOUNT:
        return Localized.dictionary().total();

      case COL_SALE:
        return Localized.dictionary().trdInvoiceId();

      case "Arr" + COL_TRADE_INVOICE_PREFIX:
      case COL_TRADE_INVOICE_PREFIX:
        return Localized.dictionary().trdInvoicePrefix();
      case "Arr" + COL_TRADE_INVOICE_NO:
      case COL_TRADE_INVOICE_NO:
        return Localized.dictionary().trdInvoiceNo();

      case COL_TA_INVOICE_FROM:
        return Localized.dictionary().dateFrom();
      case COL_TA_INVOICE_TO:
        return Localized.dictionary().dateTo();

      case ALS_ITEM_TOTAL:
        return Localized.dictionary().goods();
      case COL_TA_SERVICE_TARIFF:
        return Localized.dictionary().taTariff();

      case COL_TA_SERVICE_FACTOR:
        return Localized.dictionary().taFactorShort();
      case COL_TA_SERVICE_DAYS:
        return Localized.dictionary().taDaysPerWeekShort();
      case COL_TA_SERVICE_MIN:
        return Localized.dictionary().taMinTermShort();

      case "Arr" + COL_TRADE_AMOUNT:
        return "Suma EUR sąsk.";

      case "ArrInvoiceDate" :
        return "Sąsk. data";

      case "ArrSaleItemDiscount":
        return "Nuolaida sąsk. %";

      case "SaleFactor":
        return "Sąsk. tarifas %";

      case COL_EXTERNAL_STOCK:
        return "ERP likutis";

      default:
        logger.warning(NameUtils.getClassName(TradeActHelper.class), name, "label not defined");
        return name;
    }
  }

  static String getLabel(String name, boolean plural) {
    switch (name) {
      case COL_TA_COMPANY:
        return plural ? Localized.dictionary().clients() : Localized.dictionary().client();

      case COL_TA_OBJECT:
        return plural ? Localized.dictionary().objects() : Localized.dictionary().object();

      case COL_TA_OPERATION:
        return plural ? Localized.dictionary().trdOperationsShort()
            : Localized.dictionary().trdOperation();

      case COL_TA_STATUS:
        return plural ? Localized.dictionary().trdStatuses() : Localized.dictionary().status();

      case COL_TA_SERIES:
        return plural ? Localized.dictionary().seriesPlural()
            : Localized.dictionary().trdSeries();

      case COL_TA_MANAGER:
        return plural ? Localized.dictionary().managers() : Localized.dictionary().manager();

      case COL_WAREHOUSE:
        return plural ? Localized.dictionary().warehouses()
            : Localized.dictionary().warehouse();

      case COL_CATEGORY:
        return plural ? Localized.dictionary().categories() : Localized.dictionary().category();

      case COL_TA_ITEM:
        return plural ? Localized.dictionary().goods() : Localized.dictionary().item();

      case COL_TRADE_SUPPLIER:
        return plural ? Localized.dictionary().suppliers() : Localized.dictionary().supplier();

      default:
        logger.warning(NameUtils.getClassName(TradeActHelper.class), name, plural,
            "label not defined");
        return name;
    }
  }

  static NumberFormat getNumberFormat(String name) {
    switch (name) {
      case COL_TRADE_ACT:
      case COL_TA_ITEM:
      case COL_SALE:
        return Format.getDefaultLongFormat();

      case COL_TRADE_ITEM_QUANTITY:
      case TradeActConstants.ALS_RETURNED_QTY:
      case ALS_REMAINING_QTY:
      case COL_EXTERNAL_STOCK:
        return getQuantityFormat();

      case COL_TRADE_ITEM_PRICE:
      case "SaleFactor":
        return getPriceFormat();

      case COL_TRADE_DISCOUNT:
        case "SaleItemDiscount":
        return getDiscountPercentFormat();

      case ALS_BASE_AMOUNT:
      case ALS_DISCOUNT_AMOUNT:
      case ALS_WITHOUT_VAT:
      case ALS_VAT_AMOUNT:
      case ALS_TOTAL_AMOUNT:
      case ALS_ITEM_TOTAL:
      case COL_COST_AMOUNT:
      case COL_TRADE_AMOUNT:
        return getAmountFormat();

      case COL_ITEM_WEIGHT:
        return getWeightFormat();

      case COL_TA_SERVICE_DAYS:
      case COL_TA_SERVICE_MIN:
        return Format.getDefaultIntegerFormat();

      case COL_TA_SERVICE_TARIFF:
      case COL_TA_SERVICE_FACTOR:
        return getDecimalFormat(VIEW_TRADE_ACT_SERVICES, name);

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

  static NumberFormat getWeightFormat() {
    if (weightFormat == null) {
      Integer scale = Data.getColumnScale(VIEW_ITEMS, COL_ITEM_WEIGHT);
      weightFormat = Format.getDecimalFormat(BeeUtils.unbox(scale));
    }
    return weightFormat;
  }

  static ValueType getType(Collection<String> viewNames, String colName) {
    switch (colName) {
      case TradeActConstants.ALS_RETURNED_QTY:
      case ALS_REMAINING_QTY:

      case ALS_BASE_AMOUNT:
      case ALS_DISCOUNT_AMOUNT:
      case ALS_WITHOUT_VAT:
      case ALS_VAT_AMOUNT:
      case ALS_TOTAL_AMOUNT:
      case ALS_ITEM_TOTAL:
      case COL_COST_AMOUNT:
      case COL_TRADE_AMOUNT:
      case "SaleItemDiscount":
      case "SaleFactor":
        return ValueType.NUMBER;

      case ALS_WAREHOUSE_CODE:
      case ALS_ITEM_TYPE_NAME:
      case ALS_ITEM_GROUP_NAME:
      case ALS_COMPANY_NAME:
      case COL_FIRST_NAME:
      case COL_LAST_NAME:
      case COL_TRADE_SALE_SERIES:
      case COL_TRADE_INVOICE_PREFIX:
      case COL_TRADE_INVOICE_NO:
        return ValueType.TEXT;

      case "InvoiceDate":
        return ValueType.DATE;
    }

    if (colName.startsWith(PFX_START_STOCK)
        || colName.startsWith(PFX_MOVEMENT)
        || colName.startsWith(PFX_END_STOCK)) {
      return ValueType.NUMBER;
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
