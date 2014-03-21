package com.butent.bee.shared.modules.service;

public final class ServiceConstants {

  public static final String SVC_CREATE_INVOICE_ITEMS = "CreateInvoiceItems";
  
  public static final String TBL_MAINTENANCE = "Maintenance";

  public static final String VIEW_OBJECTS = "ServiceObjects";

  public static final String VIEW_CRITERIA_GROUPS = "ServiceCritGroups";
  public static final String VIEW_CRITERIA = "ServiceCriteria";

  public static final String VIEW_DISTINCT_CRITERIA = "ServiceDistinctCriteria";
  public static final String VIEW_DISTINCT_VALUES = "ServiceDistinctCritValues";

  public static final String VIEW_OBJECT_CRITERIA = "ServiceObjectCriteria";

  public static final String VIEW_OBJECT_FILES = "ServiceFiles";

  public static final String VIEW_MAINTENANCE = "Maintenance";
  public static final String VIEW_INVOICES = "ServiceInvoices";

  public static final String COL_OBJECT_CATEGORY = "Category";
  public static final String COL_OBJECT_CUSTOMER = "Customer";
  
  public static final String COL_CRITERIA_GROUP = "Group";
  public static final String COL_CRITERIA_GROUP_NAME = "Name";
  public static final String COL_CRITERION_NAME = "Criterion";
  public static final String COL_CRITERION_VALUE = "Value";
  public static final String COL_CRITERIA_ORDINAL = "Ordinal";
  
  public static final String COL_SERVICE_OBJECT = "ServiceObject";
  
  public static final String COL_CATEGORY_NAME = "Name";

  public static final String COL_MAINTENANCE_DATE = "Date";
  public static final String COL_MAINTENANCE_ITEM = "Item";
  public static final String COL_MAINTENANCE_QUANTITY = "Quantity";
  public static final String COL_MAINTENANCE_PRICE = "Price";
  public static final String COL_MAINTENANCE_CURRENCY = "Currency";
  public static final String COL_MAINTENANCE_INVOICE = "Invoice";

  public static final String ALS_CATEGORY_NAME = "CategoryName";
  public static final String ALS_ITEM_NAME = "ItemName";
  public static final String ALS_CUSTOMER_NAME = "CustomerName";

  public static final String PROP_MAIN_ITEM = "MainItem";

  public static final String STYLE_SHEET = "service";
  public static final String STYLE_PREFIX = "bee-svc-";
  
  private ServiceConstants() {
  }
}
