package com.butent.bee.shared.modules.orders;

import com.butent.bee.shared.i18n.Dictionary;
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
    };

    public boolean is(Integer status) {
      return status != null && ordinal() == status;
    }
  }

  public static void register() {
    EnumUtils.register(OrdersStatus.class);
  }

  public static final String SVC_GET_TEMPLATE_ITEMS = "GetTemplateItems";
  public static final String SVC_CREATE_INVOICE_ITEMS = "CreateInvoiceItems";
  public static final String SVC_GET_NEXT_NUMBER = "GetNextNumber";
  public static final String SVC_EXPORT_ITEM_REMAINDERS = "ExportItemReminder";
  public static final String SVC_CREATE_PDF_FILE = "CreatePDFFile";
  public static final String SVC_FILL_RESERVED_REMAINDERS = "FillReservedRemainders";
  public static final String SVC_GET_ITEMS_SUPPLIERS = "GetItemsSuppliers";
  public static final String SVC_GET_FILTERED_INVOICES = "GetFilteredInvoices";
  public static final String SVC_GET_ERP_STOCKS = "GetERPStocks";
  public static final String SVC_GET_CREDIT_INFO = "GetCreditInfo";

  public static final String SVC_GET_CONFIGURATION = "GetConfiguration";
  public static final String SVC_SAVE_DIMENSIONS = "SaveDimensions";
  public static final String SVC_SET_BUNDLE = "SetBundle";
  public static final String SVC_DELETE_BUNDLES = "DeleteBundles";
  public static final String SVC_SET_OPTION = "SetOption";
  public static final String SVC_DELETE_OPTION = "DeleteOption";
  public static final String SVC_SET_RELATION = "SetRelation";
  public static final String SVC_DELETE_RELATION = "DeleteRelation";
  public static final String SVC_SET_RESTRICTIONS = "SetRestrictions";
  public static final String SVC_SAVE_OBJECT = "SaveObject";
  public static final String SVC_GET_OBJECT = "GetObject";

  public static final String TBL_ORDER_ITEMS = "OrderItems";
  public static final String TBL_ORDERS = "Orders";

  public static final String TBL_CONF_GROUPS = "ConfGroups";
  public static final String TBL_CONF_OPTIONS = "ConfOptions";
  public static final String TBL_CONF_PRICELIST = "ConfPricelist";
  public static final String TBL_CONF_DIMENSIONS = "ConfDimensions";
  public static final String TBL_CONF_BUNDLES = "ConfBundles";
  public static final String TBL_CONF_BUNDLE_OPTIONS = "ConfBundleOptions";
  public static final String TBL_CONF_BRANCH_BUNDLES = "ConfBranchBundles";
  public static final String TBL_CONF_BRANCH_OPTIONS = "ConfBranchOptions";
  public static final String TBL_CONF_RELATIONS = "ConfRelations";
  public static final String TBL_CONF_RESTRICTIONS = "ConfRestrictions";
  public static final String TBL_CONF_OBJECTS = "ConfObjects";
  public static final String TBL_CONF_OBJECT_OPTIONS = "ConfObjectOptions";

  public static final String VIEW_ORDERS = "Orders";
  public static final String VIEW_ORDER_CHILD_INVOICES = "OrderChildInvoices";
  public static final String VIEW_ORDER_ITEMS = "OrderItems";
  public static final String VIEW_ORDERS_TEMPLATES = "OrdersTemplates";
  public static final String VIEW_ORDER_TMPL_ITEMS = "OrderTmplItems";
  public static final String VIEW_ORDER_SALES = "OrderSales";

  public static final String FORM_NEW_ORDER_INVOICE = "NewOrderInvoice";

  public static final String GRID_COMPANY_ORDERS = "CompanyOrders";
  public static final String GRID_OFFERS = "Offers";
  public static final String GRID_ORDERS_INVOICES = "OrdersInvoices";

  public static final String ALS_COMPANY_EMAIL = "CompanyEmail";
  public static final String ALS_CONTACT_EMAIL = "ContactEmail";
  public static final String ALS_TOTAL_QTY = "TotalQty";
  public static final String ALS_MANAGER_FIRST_NAME = "ManagerFirstName";
  public static final String ALS_MANAGER_LAST_NAME = "ManagerLastName";
  public static final String ALS_CUSTOMER_EMAIL = "CustomerEmail";
  public static final String ALS_PAYER_EMAIL = "PayerEmail";
  public static final String ALS_RESERVATIONS = "Reservations";

  public static final String COL_END_DATE = "EndDate";
  public static final String COL_ORDER = "Order";
  public static final String COL_ORDERS_STATUS = "Status";
  public static final String COL_RESERVED_REMAINDER = "ResRemainder";
  public static final String COL_TEMPLATE = "Template";
  public static final String COL_SOURCE = "Source";
  public static final String COL_UNPACKING = "Unpacking";
  public static final String COL_ORDER_ITEM = "OrderItem";
  public static final String COL_COMPLETED_QTY = "CompletedQty";
  public static final String COL_SUPPLIER_TERM = "SupplierTerm";
  public static final String COL_INVISIBLE_DISCOUNT = "InvisibleDiscount";

  public static final String COL_BRANCH = "Branch";
  public static final String COL_OPTION = "Option";
  public static final String COL_OPTION_NAME = "OptionName";
  public static final String COL_CODE = "Code";
  public static final String COL_DESCRIPTION = "Description";
  public static final String COL_BUNDLE = "Bundle";
  public static final String COL_KEY = "Key";
  public static final String COL_GROUP = "Group";
  public static final String COL_GROUP_NAME = "GroupName";
  public static final String COL_VALID_UNTIL = "ValidUntil";
  public static final String COL_ORDINAL = "Ordinal";
  public static final String COL_REQUIRED = "Required";
  public static final String COL_BRANCH_BUNDLE = "BranchBundle";
  public static final String COL_BRANCH_OPTION = "BranchOption";
  public static final String COL_DENIED = "Denied";
  public static final String COL_OBJECT = "Object";

  public static final String PRP_FREE_REMAINDER = "FreeRemainder";
  public static final String PRP_COMPLETED_INVOICES = "CompletedInvoices";
  public static final String PRP_SUPPLIER_TERM = "SupplierTerm";
  public static final String PRP_AMOUNT_WO_VAT = "AmountWoVAT";
  public static final String PRM_MANAGER_WAREHOUSE = "ManagerWarehouse";

  public static final String PRM_AUTO_RESERVATION = "AutoReservation";
  public static final String PRM_UPDATE_ITEMS_PRICES = "UpdateItemsPrices";
  public static final String PRM_IMPORT_ERP_ITEMS_TIME = "ImportERPItemsTime";
  public static final String PRM_IMPORT_ERP_STOCKS_TIME = "ImportERPStocksTime";
  public static final String PRM_EXPORT_ERP_RESERVATIONS_TIME = "ExportERPReservationsTime";
  public static final String PRM_DEFAULT_SALE_OPERATION = "DefaultSaleOperation";
  public static final String PRM_CLEAR_RESERVATIONS_TIME = "ClearReservationsTime";
  public static final String PRM_MANAGER_DISCOUNT = "ManagerDiscount";
  public static final String PRM_CHECK_DEBT = "CheckDebt";
  public static final String PRM_NOTIFY_ABOUT_DEBTS = "NotifyAboutDebts";

  public static final String FORM_CONF_OPTION = "ConfOption";

  private OrdersConstants() {
  }
}
