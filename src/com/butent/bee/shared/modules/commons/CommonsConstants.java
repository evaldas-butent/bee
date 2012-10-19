package com.butent.bee.shared.modules.commons;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.ui.HasCaption;

import java.util.EnumSet;
import java.util.Set;

public class CommonsConstants {

  public static enum RightsObjectType implements HasCaption {
    EVENT("Įvykis", EnumSet.of(RightsState.ENABLED)),
    FORM("Forma", EnumSet.of(RightsState.VISIBLE, RightsState.ENABLED)),
    GRID("Lentelė", EnumSet.of(RightsState.VISIBLE, RightsState.ENABLED)),
    MENU("Meniu", EnumSet.of(RightsState.VISIBLE)),
    MODULE("Modulis", EnumSet.of(RightsState.VISIBLE));

    private final String caption;
    private final Set<RightsState> registeredStates;

    private RightsObjectType(String caption, Set<RightsState> states) {
      this.caption = caption;
      this.registeredStates = states;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public Set<RightsState> getRegisteredStates() {
      return registeredStates;
    }
  }

  public static enum RightsState implements HasCaption {
    VISIBLE("Matomas", true),
    ENABLED("Leidžiamas", true),
    MARKED("Pažymėtas", true);

    private final String caption;
    private final boolean checked;

    private RightsState(String caption, boolean checked) {
      this.caption = caption;
      this.checked = checked;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public boolean isChecked() {
      return checked;
    }
  }

  public static final String COMMONS_MODULE = "Commons";
  public static final String COMMONS_METHOD = COMMONS_MODULE + "Method";

  public static final String COMMONS_PARAMETERS_PREFIX = "parameters_";

  public static final String SVC_GET_PARAMETERS = COMMONS_PARAMETERS_PREFIX + "get";
  public static final String SVC_CREATE_PARAMETER = COMMONS_PARAMETERS_PREFIX + "save";
  public static final String SVC_REMOVE_PARAMETERS = COMMONS_PARAMETERS_PREFIX + "remove";
  public static final String SVC_SET_PARAMETER = COMMONS_PARAMETERS_PREFIX + "set";
  public static final String VAR_PARAMETERS_MODULE = Service.RPC_VAR_PREFIX + "module";
  public static final String VAR_PARAMETERS = Service.RPC_VAR_PREFIX + "parameters";
  public static final String VAR_PARAMETER_VALUE = Service.RPC_VAR_PREFIX + "value";

  public static final String COMMONS_ITEM_PREFIX = "item_";

  public static final String VAR_ITEM_ID = Service.RPC_VAR_PREFIX + "item_id";
  public static final String VAR_ITEM_CATEGORIES = Service.RPC_VAR_PREFIX + "item_categories";
  public static final String VAR_ITEM_DATA = Service.RPC_VAR_PREFIX + "item_data";

  public static final String SVC_ITEM_CREATE = COMMONS_ITEM_PREFIX + "create";
  public static final String SVC_ADD_CATEGORIES = COMMONS_ITEM_PREFIX + "AddCategories";
  public static final String SVC_REMOVE_CATEGORIES = COMMONS_ITEM_PREFIX + "RemoveCategories";

  public static final String TBL_FILES = "Files";

  public static final String TBL_ITEMS = "Items";
  public static final String TBL_CATEGORIES = "CategoryTree";
  public static final String TBL_ITEM_CATEGORIES = "ItemCategories";
  public static final String TBL_CONTACTS = "Contacts";
  public static final String TBL_COMPANY_PERSONS = "CompanyPersons";

  public static final String VIEW_COMPANIES = "Companies";

  public static final String COL_NAME = "Name";
  public static final String COL_ITEM = "Item";
  public static final String COL_CATEGORY = "Category";
  public static final String COL_SERVICE = "IsService";

  public static final String COL_CODE = "Code";

  public static final String COL_CONTACT = "Contact";
  public static final String COL_PHONE = "Phone";
  public static final String COL_EMAIL = "Email";
  public static final String COL_ADDRESS = "Address";

  public static final String COL_CITY_NAME = "CityName";
  public static final String COL_COUNTRY_NAME = "CountryName";

  public static final String FORM_NEW_COMPANY = "Company";

  public static final String PRM_SQL_MESSAGES = "SQLMessages";

  private CommonsConstants() {
  }
}
