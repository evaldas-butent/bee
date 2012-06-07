package com.butent.bee.shared.modules.commons;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.ui.HasCaption;

public class CommonsConstants {

  public static enum RightsObjectType implements HasCaption {
    EVENT("Įvykis"),
    FORM("Forma"),
    GRID("Lentelė"),
    MENU("Meniu");

    private final String caption;

    private RightsObjectType(String caption) {
      this.caption = caption;
    }

    public String getCaption() {
      return caption;
    }
  }

  public static final String COMMONS_MODULE = "Commons";
  public static final String COMMONS_METHOD = COMMONS_MODULE + "Method";

  public static final String COMMONS_PARAMETERS_PREFIX = "parameters_";

  public static final String SVC_GET_PARAMETERS = COMMONS_PARAMETERS_PREFIX + "get";
  public static final String SVC_SAVE_PARAMETERS = COMMONS_PARAMETERS_PREFIX + "save";
  public static final String SVC_REMOVE_PARAMETERS = COMMONS_PARAMETERS_PREFIX + "remove";
  public static final String VAR_PARAMETERS_MODULE = Service.RPC_VAR_PREFIX + "parameters_module";
  public static final String VAR_PARAMETERS = Service.RPC_VAR_PREFIX + "parameters";

  public static final String COMMONS_ITEM_PREFIX = "item_";

  public static final String VAR_ITEM_ID = Service.RPC_VAR_PREFIX + "item_id";
  public static final String VAR_ITEM_CATEGORIES = Service.RPC_VAR_PREFIX + "item_categories";
  public static final String VAR_ITEM_DATA = Service.RPC_VAR_PREFIX + "item_data";

  public static final String SVC_ITEM_CREATE = COMMONS_ITEM_PREFIX + "create";
  public static final String SVC_ADD_CATEGORIES = COMMONS_ITEM_PREFIX + "AddCategories";
  public static final String SVC_REMOVE_CATEGORIES = COMMONS_ITEM_PREFIX + "RemoveCategories";

  public static final String TBL_ITEMS = "Items";
  public static final String TBL_CATEGORIES = "CategoryTree";
  public static final String TBL_ITEM_CATEGORIES = "ItemCategories";

  public static final String VIEW_COMPANIES = "Companies";

  public static final String COL_NAME = "Name";
  public static final String COL_ITEM = "Item";
  public static final String COL_CATEGORY = "Category";
  public static final String COL_SERVICE = "IsService";

  public static final String FORM_NEW_COMPANY = "Company";

  private CommonsConstants() {
  }
}
