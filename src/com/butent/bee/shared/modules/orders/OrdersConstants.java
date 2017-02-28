package com.butent.bee.shared.modules.orders;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.EnumUtils;

public final class OrdersConstants {
  public enum OrdersStatus implements HasLocalizedCaption {
    APPROVED {
      @Override
      public String getCaption(Dictionary constants) {
        return constants.ordApproved();
      }
    },
    CANCELED {
      @Override
      public String getCaption(Dictionary constants) {
        return constants.ordCanceled();
      }
    },
    PREPARED {
      @Override
      public String getCaption(Dictionary constants) {
        return constants.ordPrepared();
      }
    },
    SENT {
      @Override
      public String getCaption(Dictionary constants) {
        return constants.ordSent();
      }
    },
    FINISH {
      @Override
      public String getCaption(Dictionary constants) {
        return constants.prjStatusApproved();
      }
    },
    NEW {
      @Override
      public String getCaption(Dictionary constants) {
        return constants.ecOrderStatusNew();
      }
    };

    @Override
    public String getCaption() {
      return getCaption(Localized.dictionary());
    }

    public static OrdersStatus get(Integer status) {
      if (status != null && status >= 0 && status < values().length) {
        return values()[status];
      } else {
        return null;
      }
    }

    public static boolean in(int status, OrdersStatus... statuses) {
      for (OrdersStatus st : statuses) {
        if (st.ordinal() == status) {
          return true;
        }
      }
      return false;
    }

    public boolean is(Integer status) {
      return status != null && ordinal() == status;
    }
  }

  public static void register() {
    EnumUtils.register(OrdersStatus.class);
  }

  public static final String SVC_GET_TEMPLATE_ITEMS = "GetTemplateItems";
  public static final String SVC_CREATE_INVOICE_ITEMS = "CreateInvoiceItems";
  public static final String SVC_FILL_RESERVED_REMAINDERS = "FillReservedRemainders";
  public static final String SVC_GET_ERP_STOCKS = "GetERPStocks";
  public static final String SVC_GET_CREDIT_INFO = "GetCreditInfo";

  public static final String TBL_ORDER_ITEMS = "OrderItems";
  public static final String TBL_ORDERS = "Orders";
  public static final String TBL_ORDER_SERIES = "OrderSeries";
  public static final String TBL_ORD_EC_USAGE = "OrdEcUsage";

  public static final String FORM_ORD_EC_REGISTRATION = "OrdEcRegistration";

  public static final String VIEW_ORDERS = "Orders";
  public static final String VIEW_ORDER_CHILD_INVOICES = "OrderChildInvoices";
  public static final String VIEW_ORDER_ITEMS = "OrderItems";
  public static final String VIEW_ORDERS_TEMPLATES = "OrdersTemplates";
  public static final String VIEW_ORDER_TMPL_ITEMS = "OrderTmplItems";
  public static final String VIEW_ORDER_SALES = "OrderSales";
  public static final String VIEW_DISTINCT_ORDER_VALUES = "DistinctOrderValues";

  public static final String FORM_NEW_ORDER_INVOICE = "NewOrderInvoice";

  public static final String GRID_COMPANY_ORDERS = "CompanyOrders";
  public static final String GRID_OFFERS = "Offers";
  public static final String GRID_ORDERS_INVOICES = "OrdersInvoices";

  public static final String ALS_COMPANY_EMAIL = "CompanyEmail";
  public static final String ALS_CONTACT_EMAIL = "ContactEmail";
  public static final String ALS_MANAGER_FIRST_NAME = "ManagerFirstName";
  public static final String ALS_MANAGER_LAST_NAME = "ManagerLastName";
  public static final String ALS_RESERVATIONS = "Reservations";
  public static final String ALS_CUSTOMER_EMAIL = "CustomerEmail";
  public static final String ALS_PAYER_EMAIL = "PayerEmail";

  public static final String COL_END_DATE = "EndDate";
  public static final String COL_START_DATE = "StartDate";
  public static final String COL_ORDER = "Order";
  public static final String COL_ORDERS_STATUS = "Status";
  public static final String COL_RESERVED_REMAINDER = "ResRemainder";
  public static final String COL_TEMPLATE = "Template";
  public static final String COL_SOURCE = "Source";
  public static final String COL_UNPACKING = "Unpacking";
  public static final String COL_ORDER_ITEM = "OrderItem";
  public static final String COL_COMPLETED_QTY = "CompletedQty";
  public static final String COL_INVISIBLE_DISCOUNT = "InvisibleDiscount";
  public static final String COL_SALE_ITEM = "SaleItem";
  public static final String COL_ORDER_TRADE_OPERATION = "TradeOperation";
  public static final String COL_TRADE_OPERATION_NAME = "OperationName";

  public static final String PRP_FREE_REMAINDER = "FreeRemainder";
  public static final String PRP_COMPLETED_INVOICES = "CompletedInvoices";
  public static final String PRP_AMOUNT_WO_VAT = "AmountWoVAT";
  public static final String PRM_MANAGER_WAREHOUSE = "ManagerWarehouse";

  public static final String PRM_UPDATE_ITEMS_PRICES = "UpdateItemsPrices";
  public static final String PRM_IMPORT_ERP_ITEMS_TIME = "ImportERPItemsTime";
  public static final String PRM_IMPORT_ERP_STOCKS_TIME = "ImportERPStocksTime";
  public static final String PRM_EXPORT_ERP_RESERVATIONS_TIME = "ExportERPReservationsTime";
  public static final String PRM_DEFAULT_SALE_OPERATION = "DefaultSaleOperation";
  public static final String PRM_CLEAR_RESERVATIONS_TIME = "ClearReservationsTime";
  public static final String PRM_MANAGER_DISCOUNT = "ManagerDiscount";
  public static final String PRM_CHECK_DEBT = "CheckDebt";
  public static final String PRM_NOTIFY_ABOUT_DEBTS = "NotifyAboutDebts";
  public static final String PRM_IMPORT_ERP_INV_CHANGES_TIME = "ImportERPInvoiceChangesTime";
  public static final String PRM_GET_INV_DAYS_BEFORE = "GetInvDaysBefore";

  public static final String FORM_CONF_OPTION = "ConfOption";

  // E-Commerce
  public static final String SVC_EC_SEARCH_BY_ITEM_ARTICLE = "SearchByItemArticle";
  public static final String SVC_EC_SEARCH_BY_ITEM_CATEGORY = "SearchByItemCategory";
  public static final String SVC_GET_PICTURES = "GetPictures";
  public static final String SVC_GET_CATEGORIES = "GetCategories";
  public static final String SVC_GLOBAL_SEARCH = "GlobalSearch";
  public static final String SVC_GET_CLIENT_STOCK_LABELS = "GetClientStockLabel";
  public static final String SVC_EC_GET_CONFIGURATION = "EcGetConfiguration";
  public static final String SVC_EC_CLEAR_CONFIGURATION = "EcClearConfiguration";
  public static final String SVC_EC_SAVE_CONFIGURATION = "EcSaveConfiguration";
  public static final String SVC_FINANCIAL_INFORMATION = "FinancialInformation";
  public static final String SVC_UPDATE_SHOPPING_CART = "UpdateShoppingCart";
  public static final String SVC_GET_SHOPPING_CARTS = "GetShoppingCarts";
  public static final String SVC_UPLOAD_BANNERS = "UploadBanners";
  public static final String SVC_GET_PROMO = "GetPromo";
  public static final String SVC_SUBMIT_ORDER = "SubmitOrder";
  public static final String SVC_MAIL_ORDER = "MailOrder";
  public static final String SVC_EC_GET_NOT_SUBMITTED_ORDERS = "GetNotSubmittedOrders";
  public static final String SVC_SAVE_ORDER = "SaveOrder";
  public static final String SVC_EC_OPEN_SHOPPING_CART = "OpenShoppingCart";
  public static final String SVC_EC_CLEAN_SHOPPING_CART = "CleanShoppingCart";
  public static final String SVC_EC_GET_DOCUMENTS = "GetDocuments";

  public static final String VAR_QUERY = "Query";
  public static final int MIN_SEARCH_QUERY_LENGTH = 3;

  public static final String TBL_ORD_EC_SHOPPING_CARTS = "OrdEcShoppingCarts";
  public static final String TBL_ORD_EC_BANNERS = "OrdEcBanners";
  public static final String TBL_NOT_SUBMITTED_ORDERS = "NotSubmittedOrders";

  public static final String VIEW_ORD_EC_REGISTRATIONS = "OrdEcRegistrations";
  public static final String VIEW_ORD_EC_CONFIGURATION = "OrdEcConfiguration";

  public static final String COL_REGISTRATION_DATE = "Date";
  public static final String COL_REGISTRATION_TYPE = "Type";
  public static final String COL_REGISTRATION_COMPANY_NAME = "CompanyName";
  public static final String COL_REGISTRATION_COMPANY_CODE = "CompanyCode";
  public static final String COL_REGISTRATION_COMPANY_TYPE = "CompanyType";
  public static final String COL_REGISTRATION_NOT_VAT_PAYER = "NotVATPayer";
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
  public static final String COL_REGISTRATION_LANGUAGE = "Language";
  public static final String COL_REGISTRATION_HOST = "Host";
  public static final String COL_REGISTRATION_AGENT = "Agent";
  public static final String COL_REGISTRATION_REGISTERED = "Registered";

  public static final String COL_SHOPPING_CART_CREATED = "Created";
  public static final String COL_SHOPPING_CART_NAME = "Name";
  public static final String COL_SHOPPING_CART_CLIENT = "Client";
  public static final String COL_SHOPPING_CART_ITEM = "Item";
  public static final String COL_SHOPPING_CART_QUANTITY = "Quantity";
  public static final String COL_SHOPPING_CART_COMMENT = "Comment";

  public static final String COL_BANNER_PICTURE = "Picture";

  private OrdersConstants() {
  }
}
