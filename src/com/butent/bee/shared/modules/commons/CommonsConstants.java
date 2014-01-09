package com.butent.bee.shared.modules.commons;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class CommonsConstants {

  public static final List<BeeColumn> HISTORY_COLUMNS = Lists.newArrayList(
      new BeeColumn(ValueType.DATE_TIME, CommonsConstants.AUDIT_FLD_TIME, false),
      new BeeColumn(ValueType.TEXT, CommonsConstants.COL_USER, true),
      new BeeColumn(ValueType.LONG, CommonsConstants.AUDIT_FLD_TX, false),
      new BeeColumn(ValueType.TEXT, CommonsConstants.AUDIT_FLD_MODE, false),
      new BeeColumn(ValueType.TEXT, CommonsConstants.COL_OBJECT, false),
      new BeeColumn(ValueType.LONG, CommonsConstants.AUDIT_FLD_ID, false),
      new BeeColumn(ValueType.TEXT, CommonsConstants.AUDIT_FLD_FIELD, false),
      new BeeColumn(ValueType.TEXT, CommonsConstants.AUDIT_FLD_VALUE, true),
      new BeeColumn(ValueType.TEXT, CommonsConstants.COL_RELATION, true));

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

  public static void register() {
    EnumUtils.register(RightsObjectType.class);
    EnumUtils.register(RightsState.class);
    EnumUtils.register(ParameterType.class);
    EnumUtils.register(ReminderMethod.class);
    EnumUtils.register(SupportedLocale.class);
    EnumUtils.register(UserInterface.class);
    EnumUtils.register(Feed.class);
  }

  public static final String COMMONS_MODULE = "Commons";
  public static final String COMMONS_METHOD = COMMONS_MODULE + "Method";

  public static final String COMMONS_PARAMETERS_PREFIX = "parameters_";

  public static final String SVC_GET_PARAMETER = COMMONS_PARAMETERS_PREFIX + "parameter";
  public static final String SVC_GET_PARAMETERS = COMMONS_PARAMETERS_PREFIX + "get";
  public static final String SVC_RESET_PARAMETER = COMMONS_PARAMETERS_PREFIX + "reset";
  public static final String SVC_SET_PARAMETER = COMMONS_PARAMETERS_PREFIX + "set";

  public static final String SVC_NUMBER_TO_WORDS = "GetNumberInWords";

  public static final String VAR_AMOUNT = Service.RPC_VAR_PREFIX + "amount";
  public static final String VAR_LOCALE = Service.RPC_VAR_PREFIX + "locale";

  public static final String VAR_PARAMETERS_MODULE = Service.RPC_VAR_PREFIX + "module";
  public static final String VAR_PARAMETER = Service.RPC_VAR_PREFIX + "parameters";
  public static final String VAR_PARAMETER_VALUE = Service.RPC_VAR_PREFIX + "value";

  public static final String SVC_GET_HISTORY = "get_history";
  public static final String VAR_HISTORY_VIEW = Service.RPC_VAR_PREFIX + "history_view";
  public static final String VAR_HISTORY_IDS = Service.RPC_VAR_PREFIX + "history_ids";

  public static final String COMMONS_ITEM_PREFIX = "item_";

  public static final String VAR_ITEM_ID = Service.RPC_VAR_PREFIX + "item_id";
  public static final String VAR_ITEM_CATEGORIES = Service.RPC_VAR_PREFIX + "item_categories";
  public static final String VAR_ITEM_DATA = Service.RPC_VAR_PREFIX + "item_data";

  public static final String VAR_DATE_LOW = Service.RPC_VAR_PREFIX + "date_low";
  public static final String VAR_DATE_HIGH = Service.RPC_VAR_PREFIX + "date_high";

  public static final String SVC_COMPANY_INFO = "GetCompanyInfo";

  public static final String SVC_ITEM_CREATE = COMMONS_ITEM_PREFIX + "create";
  public static final String SVC_ADD_CATEGORIES = COMMONS_ITEM_PREFIX + "add_categories";
  public static final String SVC_REMOVE_CATEGORIES = COMMONS_ITEM_PREFIX + "remove_categories";

  public static final String SVC_GET_CURRENT_EXCHANGE_RATE = "get_current_exchange_rate";
  public static final String SVC_GET_EXCHANGE_RATE = "get_exchange_rate";
  public static final String SVC_GET_LIST_OF_CURRENCIES = "get_list_of_currencies";
  public static final String SVC_GET_EXCHANGE_RATES_BY_CURRENCY =
      "get_exchange_rates_by_currency";

  public static final String SVC_UPDATE_EXCHANGE_RATES = "update_exchange_rates";

  public static final String SVC_BLOCK_HOST = "block_host";
  public static final String SVC_CREATE_USER = "create_user";
  public static final String SVC_CREATE_COMPANY = "create_company";

  public static final String AUDIT_SUFFIX = "AUDIT";
  public static final String AUDIT_USER = "bee.user";
  public static final String AUDIT_FLD_TIME = "Time";
  public static final String AUDIT_FLD_USER = "UserId";
  public static final String AUDIT_FLD_TX = "TransactionId";
  public static final String AUDIT_FLD_MODE = "Mode";
  public static final String AUDIT_FLD_ID = "RecordId";
  public static final String AUDIT_FLD_FIELD = "Field";
  public static final String AUDIT_FLD_VALUE = "Value";

  public static final String TBL_USERS = "Users";
  public static final String TBL_ROLES = "Roles";
  public static final String TBL_USER_ROLES = "UserRoles";
  public static final String TBL_OBJECTS = "Objects";
  public static final String TBL_RIGHTS = "Rights";
  public static final String TBL_USER_HISTORY = "UserHistory";

  public static final String TBL_FILES = "Files";
  public static final String TBL_FILE_PARTS = "FileParts";

  public static final String TBL_ITEMS = "Items";
  public static final String TBL_UNITS = "Units";
  public static final String TBL_CATEGORIES = "CategoryTree";
  public static final String TBL_ITEM_CATEGORIES = "ItemCategories";

  public static final String TBL_CONTACTS = "Contacts";
  public static final String TBL_EMAILS = "Emails";

  public static final String TBL_COMPANY_PERSONS = "CompanyPersons";
  public static final String TBL_COMPANY_USERS = "CompanyUsers";
  public static final String TBL_COMPANIES = "Companies";
  public static final String TBL_COMPANY_TYPES = "CompanyTypes";
  public static final String TBL_PERSONS = "Persons";
  public static final String TBL_POSITIONS = "Positions";

  public static final String TBL_CITIES = "Cities";
  public static final String TBL_COUNTRIES = "Countries";

  public static final String TBL_REMINDER_TYPES = "ReminderTypes";

  public static final String TBL_RELATIONS = "Relations";
  public static final String TBL_FAVORITES = "Favorites";
  public static final String TBL_FILTERS = "Filters";

  public static final String TBL_BRANCHES = "Branches";
  public static final String TBL_WAREHOUSES = "Warehouses";

  public static final String TBL_IP_FILTERS = "IpFilters";

  public static final String TBL_CURRENCIES = "Currencies";
  public static final String TBL_CURRENCY_RATES = "CurrencyRates";

  public static final String TBL_AUTOCOMPLETE = "Autocomplete";

  public static final String VIEW_COMPANIES = "Companies";
  public static final String VIEW_COMPANY_PERSONS = "CompanyPersons";
  public static final String VIEW_PERSONS = "Persons";

  public static final String VIEW_USERS = "Users";
  public static final String VIEW_USER_FEEDS = "UserFeeds";

  public static final String VIEW_REMINDER_TYPES = "ReminderTypes";

  public static final String VIEW_COLORS = "Colors";
  public static final String VIEW_THEMES = "Themes";
  public static final String VIEW_THEME_COLORS = "ThemeColors";

  public static final String VIEW_ITEMS = "Items";
  public static final String VIEW_ITEM_CATEGORIES = "ItemCategories";

  public static final String VIEW_COUNTRIES = "Countries";

  public static final String VIEW_BRANCHES = "Branches";
  public static final String VIEW_WAREHOUSES = "Warehouses";

  public static final String VIEW_CURRENCIES = "Currencies";
  public static final String VIEW_CURRENCY_RATES = "CurrencyRates";

  public static final String VIEW_IP_FILTERS = "IpFilters";

  public static final String VIEW_AUTOCOMPLETE = "Autocomplete";

  public static final String GRID_PERSONS = "Persons";

  public static final String GRID_HISTORY = "History";

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
  public static final String COL_LOGGED_IN = "LoggedIn";
  public static final String COL_LOGGED_OUT = "LoggedOut";

  public static final String COL_ROLE_NAME = "Name";
  public static final String COL_USER = "User";
  public static final String COL_ROLE = "Role";

  public static final String COL_COMPANY = "Company";
  public static final String COL_COMPANY_NAME = "Name";
  public static final String COL_COMPANY_PERSON = "CompanyPerson";
  public static final String COL_COMPANY_TYPE = "CompanyType";
  public static final String COL_COMPANY_EXCHANGE_CODE = "ExchangeCode";
  public static final String COL_COMPANY_CREDIT_LIMIT = "CreditLimit";
  public static final String COL_COMPANY_LIMIT_CURRENCY = "LimitCurrency";
  public static final String COL_COMPANY_CREDIT_DAYS = "CreditDays";

  public static final String COL_COMPANY_USER_USER = "User";
  public static final String COL_COMPANY_USER_RESPONSIBILITY = "Responsibility";
  
  public static final String COL_PERSON = "Person";

  public static final String COL_POSITION = "Position";
  public static final String COL_POSITION_NAME = "Name";

  public static final String COL_OBJECT_TYPE = "Type";
  public static final String COL_OBJECT = "Object";
  public static final String COL_OBJECT_NAME = "Name";
  public static final String COL_STATE = "State";

  public static final String COL_FILE_NAME = "Name";
  public static final String COL_FILE_HASH = "Hash";
  public static final String COL_FILE_REPO = "Repository";
  public static final String COL_FILE_SIZE = "Size";
  public static final String COL_FILE_TYPE = "Type";
  public static final String COL_FILE_FILE = "File";
  public static final String COL_FILE_PART = "Part";

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
  public static final String COL_ITEM_PRICE = "Price";
  public static final String COL_ITEM_CURRENCY = "Currency";

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
  public static final String COL_BRANCH_PRIMARY_WAREHOUSE = "PrimaryWarehouse";

  public static final String COL_WAREHOUSE = "Warehouse";
  public static final String COL_WAREHOUSE_CODE = "Code";
  public static final String COL_WAREHOUSE_NAME = "Name";
  public static final String COL_WAREHOUSE_SUPPLIER_CODE = "SupplierCode";
  public static final String COL_WAREHOUSE_BRANCH = "Branch";

  public static final String COL_CURRENCY = "Currency";
  public static final String COL_CURRENCY_NAME = "Name";
  public static final String COL_CURRENCY_UPDATE_TAG = "UpdateTag";

  public static final String COL_CURRENCY_RATE_CURRENCY = "Currency";
  public static final String COL_CURRENCY_RATE_DATE = "Date";
  public static final String COL_CURRENCY_RATE_QUANTITY = "Quantity";
  public static final String COL_CURRENCY_RATE = "Rate";

  public static final String COL_IP_FILTER_HOST = "Host";
  public static final String COL_IP_FILTER_BLOCK_AFTER = "BlockAfter";
  public static final String COL_IP_FILTER_BLOCK_BEFORE = "BlockBefore";

  public static final String COL_AUTOCOMPLETE_USER = "User";
  public static final String COL_AUTOCOMPLETE_KEY = "Key";
  public static final String COL_AUTOCOMPLETE_VALUE = "Value";

  public static final String COL_FEED = "Feed";
  
  public static final String ALS_COMPANY_NAME = "CompanyName";
  public static final String ALS_COMPANY_CODE = "CompanyCode";
  public static final String ALS_COMPANY_TYPE = "ComapnyType";

  public static final String ALS_CITY_NAME = "CityName";
  public static final String ALS_COUNTRY_NAME = "CountryName";

  public static final String ALS_EMAIL_ID = "EmailId";

  public static final String FORM_COMPANY = "Company";
  public static final String FORM_PERSON = "Person";

  public static final String PRM_SQL_MESSAGES = "SQLMessages";
  public static final String PRM_AUDIT_OFF = "DisableAuditing";
  public static final String PRM_VAT_PERCENT = "VATPercent";

  public static final String PRM_ERP_ADDRESS = "ERPAddress";
  public static final String PRM_ERP_LOGIN = "ERPLogin";
  public static final String PRM_ERP_PASSWORD = "ERPPassword";

  public static final String PRM_WS_LB_EXCHANGE_RATES_ADDRESS = "WSLBExchangeRates";

  public static final String PRM_COMPANY_NAME = "CompanyName";
  public static final String PRM_URL = "Url";

  public static final String PROP_CATEGORIES = "CategList";
  public static final String PROP_ICON = "Icon";

  public static final String STYLE_SHEET = "commons";

  private CommonsConstants() {
  }
}
