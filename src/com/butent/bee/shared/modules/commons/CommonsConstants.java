package com.butent.bee.shared.modules.commons;

import com.butent.bee.shared.Service;

public class CommonsConstants {
  public static final String COMMONS_MODULE = "Commons";
  public static final String COMMONS_METHOD = COMMONS_MODULE + "Method";

  public static final String COMMONS_ITEM_PREFIX = "item_";

  public static final String VAR_ITEM_ID = Service.RPC_VAR_PREFIX + "item_id";
  public static final String VAR_ITEM_CATEGORIES = Service.RPC_VAR_PREFIX + "item_categories";
  public static final String VAR_ITEM_DATA = Service.RPC_VAR_PREFIX + "item_data";

  public static final String SVC_ITEM_CREATE = COMMONS_ITEM_PREFIX + "create";
  public static final String SVC_ADD_CATEGORIES = COMMONS_ITEM_PREFIX + "AddCategories";
  public static final String SVC_REMOVE_CATEGORIES = COMMONS_ITEM_PREFIX + "RemoveCategories";

  public static final String TBL_ITEMS = "Items";
  public static final String TBL_CATEGORIES = "Categories";
  public static final String TBL_ITEM_CATEGORIES = "ItemCategories";

  public static final String COL_NAME = "Name";
  public static final String COL_ITEM = "Item";
  public static final String COL_CATEGORY = "Category";
  public static final String COL_SERVICE = "IsService";

  private CommonsConstants() {
  }
}
