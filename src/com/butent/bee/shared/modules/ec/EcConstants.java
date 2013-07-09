package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskStatus;
import com.butent.bee.shared.ui.HasCaption;

public final class EcConstants {

  public enum CartType implements HasCaption {
    MAIN(Localized.getConstants().ecShoppingCartMain(),
        Localized.getConstants().ecShoppingCartMainShort()),
    ALTERNATIVE(Localized.getConstants().ecShoppingCartAlternative(),
        Localized.getConstants().ecShoppingCartAlternativeShort());

    private final String caption;
    private final String label;

    private CartType(String caption, String label) {
      this.caption = caption;
      this.label = label;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public String getLabel() {
      return label;
    }
  }

  public enum EcClientType implements HasCaption {
    COMPANY(Localized.getConstants().ecClientTypeCompany()),
    PERSON(Localized.getConstants().ecClientTypePerson());

    private final String caption;

    private EcClientType(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public enum EcOrderStatus implements HasCaption {
    NEW(Localized.getConstants().ecOrderStatusNew()),
    ACTIVE(Localized.getConstants().ecOrderStatusActive()),
    REJECTED(Localized.getConstants().ecOrderStatusRejected());

    public static boolean in(int status, TaskStatus... statuses) {
      for (TaskStatus ts : statuses) {
        if (ts.ordinal() == status) {
          return true;
        }
      }
      return false;
    }

    private final String caption;

    private EcOrderStatus(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public static final String EC_MODULE = "Ec";
  public static final String EC_METHOD = EC_MODULE + "Method";

  public static final String SVC_FEATURED_AND_NOVELTY = "featuredAndNovelty";

  public static final String SVC_FINANCIAL_INFORMATION = "financialInformation";
  public static final String SVC_SHOW_TERMS_OF_DELIVERY = "showTermsOfDelivery";
  public static final String SVC_SHOW_CONTACTS = "showContacts";

  public static final String SVC_GLOBAL_SEARCH = "globalSearch";

  public static final String SVC_GET_CATEGORIES = "getCategories";

  public static final String SVC_SEARCH_BY_ITEM_CODE = "searchByItemCode";
  public static final String SVC_SEARCH_BY_OE_NUMBER = "searchByOeNumber";
  public static final String SVC_SEARCH_BY_CAR = "searchByCar";
  public static final String SVC_SEARCH_BY_MANUFACTURER = "searchByManufacturer";

  public static final String SVC_GENERAL_ITEMS = "generalItems";
  public static final String SVC_BIKE_ITEMS = "bikeItems";

  public static final String SVC_GET_CAR_MANUFACTURERS = "getCarManufacturers";
  public static final String SVC_GET_CAR_MODELS = "getCarModels";
  public static final String SVC_GET_CAR_TYPES = "getCarTypes";

  public static final String SVC_GET_ITEMS_BY_CAR_TYPE = "getItemsByCarType";

  public static final String SVC_GET_ITEM_MANUFACTURERS = "getItemManufacturers";
  public static final String SVC_GET_ITEMS_BY_MANUFACTURER = "getItemsByManufacturer";
  public static final String SVC_GET_ITEM_ANALOGS = "getItemAnalogs";

  public static final String SVC_GET_DELIVERY_METHODS = "getDeliveryMethods";

  public static final String SVC_SUBMIT_ORDER = "submitOrder";

  public static final String SVC_GET_CONFIGURATION = "getConfiguration";
  public static final String SVC_CLEAR_CONFIGURATION = "clearConfiguration";
  public static final String SVC_SAVE_CONFIGURATION = "saveConfiguration";

  public static final String VAR_PREFIX = Service.RPC_VAR_PREFIX + "ec_";

  public static final String VAR_QUERY = VAR_PREFIX + "query";
  public static final String VAR_OFFSET = VAR_PREFIX + "offset";
  public static final String VAR_LIMIT = VAR_PREFIX + "limit";

  public static final String VAR_MANUFACTURER = VAR_PREFIX + "manufacturer";
  public static final String VAR_MODEL = VAR_PREFIX + "model";
  public static final String VAR_TYPE = VAR_PREFIX + "type";

  public static final String VAR_CART = VAR_PREFIX + "cart";

  public static final String TBL_DELIVERY_METHODS = "DeliveryMethods";
  public static final String TBL_CLIENTS = "EcClients";
  public static final String TBL_CONFIGURATION = "EcConfiguration";
  public static final String TBL_HISTORY = "EcHistory";
  public static final String TBL_MANAGERS = "EcManagers";
  public static final String TBL_ORDER_ITEMS = "EcOrderItems";
  public static final String TBL_ORDERS = "EcOrders";
  public static final String TBL_REGISTRATIONS = "EcRegistrations";
  public static final String TBL_REJECTION_REASONS = "RejectionReasons";
  public static final String TBL_SHOPPING_CARTS = "ShoppingCarts";

  public static final String TBL_TCD_ARTICLES = "TcdArticles";
  public static final String TBL_TCD_ANALOGS = "TcdAnalogs";

  public static final String TBL_TCD_CATEGORIES = "TcdCategories";
  public static final String TBL_TCD_ARTICLE_CATEGORIES = "TcdArticleCategories";

  public static final String TBL_TCD_MODELS = "TcdModels";
  public static final String TBL_TCD_TYPES = "TcdTypes";
  public static final String TBL_TCD_TYPE_ARTICLES = "TcdTypeArticles";

  public static final String TBL_TCD_ARTICLE_BRANDS = "TcdArticleBrands";
  public static final String TBL_TCD_REMAINDERS = "TcdRemainders";
  public static final String TBL_TCD_BRANDS_MAPPING = "TcdBrandsMapping";

  public static final String VIEW_DELIVERY_METHODS = "DeliveryMethods";
  public static final String VIEW_CLIENTS = "EcClients";
  public static final String VIEW_CONFIGURATION = "EcConfiguration";
  public static final String VIEW_HISTORY = "EcHistory";
  public static final String VIEW_MANAGERS = "EcManagers";
  public static final String VIEW_ORDER_ITEMS = "EcOrderItems";
  public static final String VIEW_ORDERS = "EcOrders";
  public static final String VIEW_REGISTRATIONS = "EcRegistrations";
  public static final String VIEW_REJECTION_REASONS = "RejectionReasons";

  public static final String COL_DELIVERY_METHOD_ID = "DeliveryMethodID";
  public static final String COL_DELIVERY_METHOD_NAME = "Name";
  public static final String COL_DELIVERY_METHOD_NOTES = "Notes";

  public static final String COL_CLIENT_ID = "ClientID";
  public static final String COL_CLIENT_USER = "User";
  public static final String COL_CLIENT_REGISTERED = "Registered";
  public static final String COL_CLIENT_TYPE = "Type";
  public static final String COL_CLIENT_PRIMARY_BRANCH = "PrimaryBranch";
  public static final String COL_CLIENT_SECONDARY_BRANCH = "SecondaryBranch";
  public static final String COL_CLIENT_MANAGER = "Manager";
  public static final String COL_CLIENT_PERSON_CODE = "PersonCode";
  public static final String COL_CLIENT_ACTIVITY = "Activity";
  public static final String COL_CLIENT_CREDIT_LIMIT_WARNING = "CreditLimitWarning";
  public static final String COL_CLIENT_NOTES = "Notes";

  public static final String COL_CONFIG_ID = "ConfigurationID";
  public static final String COL_CONFIG_TOD_URL = "TodUrl";
  public static final String COL_CONFIG_TOD_HTML = "TodHtml";
  public static final String COL_CONFIG_CONTACTS_URL = "ContactsUrl";
  public static final String COL_CONFIG_CONTACTS_HTML = "ContactsHtml";

  public static final String COL_HISTORY_DATE = "Date";
  public static final String COL_HISTORY_USER = "User";
  public static final String COL_HISTORY_SERVICE = "Service";
  public static final String COL_HISTORY_QUERY = "Query";
  public static final String COL_HISTORY_COUNT = "Count";
  public static final String COL_HISTORY_DURATION = "Duration";

  public static final String COL_MANAGER_USER = "User";
  public static final String COL_MANAGER_TAB_NR = "TabNr";
  public static final String COL_MANAGER_REPORT_ORDER = "ReportOrder";
  public static final String COL_MANAGER_NOTES = "Notes";

  public static final String COL_ORDER_ITEM_ORDER_ID = "Order";
  public static final String COL_ORDER_ITEM_ID = "Item";
  public static final String COL_ORDER_ITEM_QUANTITY_ORDERED = "QuantityOrdered";
  public static final String COL_ORDER_ITEM_QUANTITY_SUBMIT = "QuantitySubmit";
  public static final String COL_ORDER_ITEM_PRICE = "Price";
  public static final String COL_ORDER_ITEM_NOTE = "Note";

  public static final String COL_ORDER_DATE = "Date";
  public static final String COL_ORDER_NUMBER = "Number";
  public static final String COL_ORDER_STATUS = "Status";
  public static final String COL_ORDER_CLIENT = "Client";
  public static final String COL_ORDER_MANAGER = "Manager";
  public static final String COL_ORDER_DELIVERY_METHOD = "DeliveryMethod";
  public static final String COL_ORDER_DELIVERY_ADDRESS = "DeliveryAddress";
  public static final String COL_ORDER_COPY_BY_MAIL = "CopyByMail";
  public static final String COL_ORDER_CLIENT_COMMENT = "ClientComment";
  public static final String COL_ORDER_MANAGER_COMMENT = "ManagerComment";
  public static final String COL_ORDER_REJECTION_REASON = "RejectionReason";
  public static final String COL_ORDER_NOTES = "Notes";

  public static final String COL_REGISTRATION_DATE = "Date";
  public static final String COL_REGISTRATION_TYPE = "Type";
  public static final String COL_REGISTRATION_BRANCH = "Branch";
  public static final String COL_REGISTRATION_COMPANY_NAME = "CompanyName";
  public static final String COL_REGISTRATION_COMPANY_CODE = "CompanyCode";
  public static final String COL_REGISTRATION_VAT_CODE = "VatCode";
  public static final String COL_REGISTRATION_PERSON_CODE = "PersonCode";
  public static final String COL_REGISTRATION_FIRST_NAME = "FirstName";
  public static final String COL_REGISTRATION_LAST_NAME = "LastName";
  public static final String COL_REGISTRATION_EMAIL = "Email";
  public static final String COL_REGISTRATION_PHONE = "Phone";
  public static final String COL_REGISTRATION_CITY = "City";
  public static final String COL_REGISTRATION_ADDRESS = "Address";
  public static final String COL_REGISTRATION_POST_INDEX = "PostIndex";
  public static final String COL_REGISTRATION_COUNTRY = "Country";
  public static final String COL_REGISTRATION_ACTIVITY = "Activity";
  public static final String COL_REGISTRATION_NOTES = "Notes";

  public static final String COL_REJECTION_REASON_NAME = "Name";
  public static final String COL_REJECTION_REASON_NOTES = "Notes";

  public static final String COL_SHOPPING_CART_CREATED = "Created";
  public static final String COL_SHOPPING_CART_CLIENT = "Client";
  public static final String COL_SHOPPING_CART_TYPE = "CartType";
  public static final String COL_SHOPPING_CART_ITEM = "Item";
  public static final String COL_SHOPPING_CART_QUANTITY = "Quantity";
  public static final String COL_SHOPPING_CART_PRICE = "Price";

  public static final String COL_TCD_ARTICLE_ID = "ArticleID";
  public static final String COL_TCD_ARTICLE_NR = "ArticleNr";
  public static final String COL_TCD_ARTICLE_NAME = "ArticleName";
  public static final String COL_TCD_SUPPLIER = "Supplier";
  public static final String COL_TCD_SUPPLIER_ID = "SupplierID";

  public static final String COL_TCD_SEARCH_NR = "SearchNr";
  public static final String COL_TCD_KIND = "Kind";
  public static final String COL_TCD_ANALOG_NR = "AnalogNr";

  public static final String COL_TCD_CATEGORY_ID = "CategoryID";
  public static final String COL_TCD_PARENT_ID = "ParentID";
  public static final String COL_TCD_CATEGORY_NAME = "CategoryName";

  public static final String COL_TCD_MODEL_ID = "ModelID";
  public static final String COL_TCD_MODEL_NAME = "ModelName";
  public static final String COL_TCD_MANUFACTURER = "Manufacturer";

  public static final String COL_TCD_TYPE_ID = "TypeID";
  public static final String COL_TCD_TYPE_NAME = "TypeName";
  public static final String COL_TCD_PRODUCED_FROM = "ProducedFrom";
  public static final String COL_TCD_PRODUCED_TO = "ProducedTo";
  public static final String COL_TCD_CCM = "Ccm";
  public static final String COL_TCD_KW_FROM = "KwFrom";
  public static final String COL_TCD_KW_TO = "KwTo";
  public static final String COL_TCD_CYLINDERS = "Cylinders";
  public static final String COL_TCD_MAX_WEIGHT = "MaxWeight";
  public static final String COL_TCD_ENGINE = "Engine";
  public static final String COL_TCD_FUEL = "Fuel";
  public static final String COL_TCD_BODY = "Body";
  public static final String COL_TCD_AXLE = "Axle";

  public static final String COL_TCD_WAREHOUSE = "Warehouse";
  public static final String COL_TCD_REMAINDER = "Remainder";
  public static final String COL_TCD_PRICE = "Price";

  public static final String COL_TCD_BRAND = "Brand";
  public static final String COL_TCD_ARTICLE_BRAND = "ArticleBrand";

  public static final String COL_TCD_SUPPLIER_BRAND = "SupplierBrand";
  public static final String COL_TCD_TECDOC_BRAND = "TecDocBrand";

  public static final String ALS_TCD_ANALOG_SUPPLIER = "AnalogSupplier";

  public static final String CATEGORY_SEPARATOR = ",";

  public static final String CURRENCY = "Lt";

  public static final int MIN_SEARCH_QUERY_LENGTH = 3;

  private EcConstants() {
  }
}
