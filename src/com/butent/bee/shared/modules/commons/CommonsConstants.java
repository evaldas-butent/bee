package com.butent.bee.shared.modules.commons;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.ui.HasCaption;

import java.util.EnumSet;
import java.util.Set;

public final class CommonsConstants {

  public enum RightsObjectType implements HasCaption {
    EVENT("Įvykis", EnumSet.of(RightsState.VISIBLE)),
    FORM("Forma", EnumSet.of(RightsState.VISIBLE, RightsState.EDITABLE)),
    GRID("Lentelė", EnumSet.of(RightsState.VISIBLE, RightsState.EDITABLE)),
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

  public enum RightsState implements HasCaption {
    VISIBLE("Matomas", true),
    EDITABLE("Redaguojamas", true);

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

  public enum ReminderMethod implements HasCaption {
    EMAIL, SMS;

    @Override
    public String getCaption() {
      return this.name().toLowerCase();
    }
  }

  public static final String COMMONS_MODULE = "Commons";
  public static final String COMMONS_METHOD = COMMONS_MODULE + "Method";

  public static final String COMMONS_PARAMETERS_PREFIX = "parameters_";

  public static final String SVC_GET_PARAMETER = COMMONS_PARAMETERS_PREFIX + "parameter";
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

  public static final String SVC_COMPANY_INFO = "GetCompanyInfo";

  public static final String SVC_ITEM_CREATE = COMMONS_ITEM_PREFIX + "create";
  public static final String SVC_ADD_CATEGORIES = COMMONS_ITEM_PREFIX + "add_categories";
  public static final String SVC_REMOVE_CATEGORIES = COMMONS_ITEM_PREFIX + "remove_categories";

  public static final String TBL_USERS = "Users";
  public static final String TBL_ROLES = "Roles";
  public static final String TBL_USER_ROLES = "UserRoles";
  public static final String TBL_OBJECTS = "Objects";
  public static final String TBL_RIGHTS = "Rights";

  public static final String TBL_FILES = "Files";

  public static final String TBL_ITEMS = "Items";
  public static final String TBL_UNITS = "Units";
  public static final String TBL_CATEGORIES = "CategoryTree";
  public static final String TBL_ITEM_CATEGORIES = "ItemCategories";
  public static final String TBL_CONTACTS = "Contacts";
  public static final String TBL_EMAILS = "Emails";
  public static final String TBL_COMPANY_PERSONS = "CompanyPersons";
  public static final String TBL_COMPANIES = "Companies";
  public static final String TBL_COMPANY_TYPES = "CompanyTypes";
  public static final String TBL_PERSONS = "Persons";
  public static final String TBL_CITIES = "Cities";
  public static final String TBL_COUNTRIES = "Countries";

  public static final String TBL_REMINDER_TYPES = "ReminderTypes";

  public static final String TBL_RELATIONS = "Relations";
  public static final String TBL_FAVORITES = "Favorites";
  public static final String TBL_FILTERS = "Filters";

  public static final String TBL_BRANCHES = "Branches";
  public static final String TBL_WAREHOUSES = "Warehouses";

  public static final String VIEW_COMPANIES = "Companies";
  public static final String VIEW_COMPANY_PERSONS = "CompanyPersons";
  public static final String VIEW_USERS = "Users";
  public static final String VIEW_PERSONS = "Persons";

  public static final String VIEW_REMINDER_TYPES = "ReminderTypes";

  public static final String VIEW_COLORS = "Colors";
  public static final String VIEW_THEMES = "Themes";
  public static final String VIEW_THEME_COLORS = "ThemeColors";

  public static final String VIEW_ITEMS = "Items";
  public static final String VIEW_ITEM_CATEGORIES = "ItemCategories";

  public static final String VIEW_COUNTRIES = "Countries";

  public static final String GRID_PERSONS = "Persons";

  public static final String COL_RELATION = "Relation";

  public static final String COL_LOGIN = "Login";
  public static final String COL_PASSWORD = "Password";
  public static final String COL_USER_PROPERTIES = "Properties";
  public static final String COL_USER_LOCALE = "Locale";
  public static final String COL_USER_INTERFACE = "Interface";
  public static final String COL_USER_BLOCK_AFTER = "BlockAfter";
  public static final String COL_USER_BLOCK_BEFORE = "BlockBefore";
  public static final String COL_REMOTE_HOST = "Host";
  public static final String COL_USER_AGENT = "Agent";

  public static final String COL_ROLE_NAME = "Name";
  public static final String COL_USER = "User";
  public static final String COL_ROLE = "Role";

  public static final String COL_COMPANY = "Company";
  public static final String COL_COMPANY_NAME = "Name";
  public static final String COL_COMPANY_PERSON = "CompanyPerson";
  public static final String COL_COMPANY_TYPE = "CompanyType";
  public static final String COL_PERSON = "Person";

  public static final String COL_OBJECT_TYPE = "Type";
  public static final String COL_OBJECT = "Object";
  public static final String COL_OBJECT_NAME = "Name";
  public static final String COL_STATE = "State";

  public static final String COL_FILE_NAME = "Name";
  public static final String COL_FILE_HASH = "Hash";
  public static final String COL_FILE_REPO = "Repository";
  public static final String COL_FILE_SIZE = "Size";
  public static final String COL_FILE_TYPE = "Type";

  public static final String COL_EMAIL_ADDRESS = "Email";
  public static final String COL_EMAIL_LABEL = "Label";

  public static final String COL_FIRST_NAME = "FirstName";
  public static final String COL_LAST_NAME = "LastName";
  public static final String COL_PHOTO = "Photo";

  public static final String COL_NAME = "Name";
  public static final String COL_CATEGORY = "Category";

  public static final String COL_ITEM = "Item";
  public static final String COL_ITEM_NAME = "Name";
  public static final String COL_ITEM_ARTICLE = "Article";
  public static final String COL_ITEM_BARCODE = "Barcode";
  public static final String COL_ITEM_IS_SERVICE = "IsService";
  public static final String COL_ITEM_EXTERNAL_CODE = "ExternalCode";

  public static final String COL_UNIT = "Unit";
  public static final String COL_UNIT_NAME = "Name";

  public static final String COL_COMPANY_CODE = "Code";
  public static final String COL_COMPANY_VAT_CODE = "VATCode";

  public static final String COL_CONTACT = "Contact";
  public static final String COL_PHONE = "Phone";
  public static final String COL_MOBILE = "Mobile";
  public static final String COL_FAX = "Fax";
  public static final String COL_EMAIL = "Email";
  public static final String COL_ADDRESS = "Address";
  public static final String COL_POST_INDEX = "PostIndex";

  public static final String COL_CITY = "City";
  public static final String COL_CITY_NAME = "Name";
  public static final String COL_COUNTRY = "Country";
  public static final String COL_COUNTRY_NAME = "Name";
  public static final String COL_COUNTRY_CODE = "Code";

  public static final String COL_REMINDER_METHOD = "Method";
  public static final String COL_REMINDER_TEMPLATE_CAPTION = "Caption";
  public static final String COL_REMINDER_TEMPLATE = "Template";

  public static final String COL_COLOR = "Color";
  public static final String COL_DEFAULT_COLOR = "DefaultColor";
  public static final String COL_BACKGROUND = "Background";
  public static final String COL_FOREGROUND = "Foreground";

  public static final String COL_THEME = "Theme";

  public static final String COL_FAVORITE_USER = "User";

  public static final String COL_FILTER_USER = "User";
  public static final String COL_FILTER_KEY = "Key";
  public static final String COL_FILTER_ORDINAL = "Ordinal";

  public static final String COL_BRANCH_NAME = "Name";
  public static final String COL_BRANCH_CODE = "Code";

  public static final String COL_WAREHOUSE = "Warehouse";
  public static final String COL_WAREHOUSE_CODE = "Code";
  public static final String COL_WAREHOUSE_SUPPLIER_CODE = "SupplierCode";
  public static final String COL_WAREHOUSE_BRANCH = "Branch";

  public static final String ALS_COMPANY_NAME = "CompanyName";
  public static final String ALS_COMPANY_TYPE = "ComapnyType";

  public static final String FORM_NEW_COMPANY = "Company";
  public static final String FORM_PERSON = "Person";

  public static final String PRM_SQL_MESSAGES = "SQLMessages";
  public static final String PRM_AUDIT_OFF = "DisableAuditing";
  public static final String PRM_VAT_PERCENT = "VATPercent";

  public static final String PRM_ERP_ADDRESS = "ERPAddress";
  public static final String PRM_ERP_LOGIN = "ERPLogin";
  public static final String PRM_ERP_PASSWORD = "ERPPassword";

  public static final String PROP_CATEGORIES = "CategList";

  private CommonsConstants() {
  }
}
