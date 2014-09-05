package com.butent.bee.shared.modules.trade.acts;

import com.butent.bee.shared.utils.EnumUtils;

public final class TradeActConstants {

  public static final String SVC_GET_ITEMS_FOR_SELECTION = "GetItemsForSelection";
  public static final String SVC_COPY_ACT = "CopyAct";
  public static final String SVC_SAVE_ACT_AS_TEMPLATE = "SaveActAsTemplate";

  public static final String TBL_TRADE_ACTS = "TradeActs";
  public static final String TBL_TRADE_ACT_ITEMS = "TradeActItems";
  public static final String TBL_TRADE_ACT_SERVICES = "TradeActServices";

  public static final String TBL_TRADE_ACT_TEMPLATES = "TradeActTemplates";
  public static final String TBL_TRADE_ACT_TMPL_ITEMS = "TradeActTmplItems";
  public static final String TBL_TRADE_ACT_TMPL_SERVICES = "TradeActTmplServices";

  public static final String VIEW_TRADE_ACTS = "TradeActs";
  public static final String VIEW_TRADE_ACT_ITEMS = "TradeActItems";
  public static final String VIEW_TRADE_ACT_SERVICES = "TradeActServices";

  public static final String VIEW_TRADE_ACT_TEMPLATES = "TradeActTemplates";
  public static final String VIEW_TRADE_ACT_TMPL_ITEMS = "TradeActTmplItems";
  public static final String VIEW_TRADE_ACT_TMPL_SERVICES = "TradeActTmplServices";

  public static final String COL_TRADE_ACT = "TradeAct";
  public static final String COL_TRADE_ACT_TEMPLATE = "TradeActTemplate";

  public static final String COL_TA_DATE = "Date";
  public static final String COL_TA_UNTIL = "Until";
  public static final String COL_TA_SERIES = "Series";
  public static final String COL_TA_NUMBER = "Number";
  public static final String COL_TA_KIND = "Kind";
  public static final String COL_TA_OPERATION = "Operation";
  public static final String COL_TA_STATUS = "Status";
  public static final String COL_TA_COMPANY = "Company";
  public static final String COL_TA_OBJECT = "ServiceObject";
  public static final String COL_TA_MANAGER = "Manager";
  public static final String COL_TA_CURRENCY = "Currency";
  public static final String COL_TA_VEHICLE = "Vehicle";
  public static final String COL_TA_DRIVER = "Driver";
  public static final String COL_TA_NOTES = "Notes";

  public static final String COL_TA_TEMPLATE_NAME = "Template";

  public static final String COL_TA_ITEM = "Item";

  public static final String COL_TA_SERVICE_FROM = "DateFrom";
  public static final String COL_TA_SERVICE_TO = "DateTo";
  public static final String COL_TA_SERVICE_TARIFF = "Tariff";
  public static final String COL_TA_SERVICE_FACTOR = "Factor";
  public static final String COL_TA_SERVICE_DAYS = "DaysPerWeek";
  public static final String COL_TA_SERVICE_MIN = "MinTerm";

  public static final String GRID_TRADE_ACTS = "TradeActs";
  public static final String GRID_TRADE_ACT_TEMPLATES = "TradeActTemplates";

  public static final String GRID_TRADE_ACT_ITEMS = "TradeActItems";
  public static final String GRID_TRADE_ACT_SERVICES = "TradeActServices";

  public static final String FORM_TRADE_ACT = "TradeAct";

  public static final String PRP_QUANTITY = "qty";
  public static final String PRP_WAREHOUSE_PREFIX = "w-";

  public static final String PRM_IMPORT_TA_ITEM_RX = "ImportActItemRegEx";
  public static final String RX_IMPORT_ACT_ITEM = "^(.+);(.*);(\\d+\\.*\\d*)$";

  public static void register() {
    EnumUtils.register(TradeActKind.class);
    EnumUtils.register(TradeActTimeUnit.class);
  }

  private TradeActConstants() {
  }
}
