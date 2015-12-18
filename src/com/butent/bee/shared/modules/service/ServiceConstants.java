package com.butent.bee.shared.modules.service;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.EnumUtils;

public final class ServiceConstants {

  public enum ServiceCompanyKind implements HasLocalizedCaption {
      CUSTOMER {
        @Override
        public String getCaption(LocalizableConstants constants) {
          return constants.customer();
        }
      },
      CONTRACTOR {
        @Override
        public String getCaption(LocalizableConstants constants) {
          return constants.svcContractor();
        }
      };

    public static final ServiceCompanyKind DETAULT = CUSTOMER;

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }
  }

  public enum ServiceFilterDataType implements HasCaption {
      CATEGORY(Localized.getConstants().category()),
      ADDRESS(Localized.getConstants().address()),
      CUSTOMER(Localized.getConstants().customer()),
      CONTRACTOR(Localized.getConstants().svcContractor());

    private final String caption;

    ServiceFilterDataType(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }

  }

  public enum SvcObjectStatus implements HasLocalizedCaption {
      /* 0 */
      SERVICE_OBJECT(Localized.getConstants().svcObjects(),
        Localized.getConstants().svcActionToServiceObjects(),
        Localized.getConstants().svcCalendar()) {
        @Override
        public String getCaption(LocalizableConstants constants) {
          return constants.svcObject();
        }
      },

      /* 1 */
      PROJECT_OBJECT(Localized.getConstants().svcProjectObjects(),
        Localized.getConstants().svcActionToProjectObjects(),
        Localized.getConstants().svcObjectCalendar()) {
        @Override
        public String getCaption(LocalizableConstants constants) {
          return constants.svcProjectObject();
        }
      },

      /* 2 */
      POTENTIAL_OBJECT(Localized.getConstants().svcObjectsPotential(),
        null,
        Localized.getConstants().svcObjectCalendar()) {
        @Override
        public String getCaption(LocalizableConstants constants) {
          return constants.svcObjectPotential();
        }
      },

      /* 3 */
      LOST_OBJECT(Localized.getConstants().svcObjectsLost(),
        Localized.getConstants().svcActionToLostObjects(),
        Localized.getConstants().svcObjectCalendar()) {
        @Override
        public String getCaption(LocalizableConstants constants) {
          return constants.svcObjectLost();
        }
      },

      /* 4 */
      TEMPLATE_OBJECT(Localized.getConstants().svcObjectsTemplate(),
        Localized.getConstants().svcActionToLostObjects(),
        Localized.getConstants().svcObjectCalendar()) {
        @Override
        public String getCaption(LocalizableConstants constants) {
          return constants.svcObjectLost();
        }
      };

    private final String listCaption;
    private final String commandCaption;
    private final String calendarCaption;

    SvcObjectStatus(String listCaption, String commandCaption, String calendarCaption) {
      this.listCaption = listCaption;
      this.commandCaption = commandCaption;
      this.calendarCaption = calendarCaption;
    }

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }

    public String getCalendarCaption() {
      return this.calendarCaption;
    }

    public String getCommandCaption() {
      return commandCaption;
    }

    public String getListCaption() {
      return listCaption;
    }

    public String getSuplierKey() {
      return GRID_SERVICE_OBJECTS + BeeConst.STRING_UNDER + name().toLowerCase();
    }

    public boolean is(Integer status) {
      return status != null && ordinal() == status;
    }
  }

  public static final String PRM_SVC_POTENTIAL_OBJECT_CATEGORY = "PotentialObjectDocumentCat";
  public static final String PRM_SVC_PROJECT_OBJECT_CATEGORY = "ProjectObjectDocumentCat";
  public static final String PRM_SVC_SERVICE_OBJECT_CATEGORY = "ServiceObjectDocumentCat";

  public static final String SVC_CREATE_INVOICE_ITEMS = "CreateInvoiceItems";
  public static final String SVC_CREATE_DEFECT_ITEMS = "CreateDefectItems";
  public static final String SVC_GET_CALENDAR_DATA = "getServiceCalendarData";
  public static final String SVC_COPY_DOCUMENT_CRITERIA = "CopyDocumentCriteria";
  public static final String SVC_COPY_OBJECT_DATA = "CopyServiceObjectData";

  public static final String VAR_SERVICE_OBJECT_STATUS = "ServiceObjectStatus";
  public static final String VAR_SERVICE_FILTER_DATA = "FilterData";
  public static final String VAR_SERVICE_TEMPLATE = "Template";

  public static final String TBL_SERVICE_TREE = "ServiceTree";
  public static final String TBL_SERVICE_OBJECTS = "ServiceObjects";
  public static final String TBL_MAINTENANCE = "Maintenance";
  public static final String TBL_SERVICE_DATES = "ServiceDates";
  public static final String TBL_SERVICE_SETTINGS = "ServiceSettings";
  public static final String TBL_SERVICE_DEFECT_ITEMS = "ServiceDefectItems";
  public static final String TBL_SERVICE_CRITERIA_GROUPS = "ServiceCritGroups";
  public static final String TBL_SERVICE_CRITERIA = "ServiceCriteria";

  public static final String VIEW_SERVICE_OBJECTS = "ServiceObjects";

  public static final String VIEW_SERVICE_CRITERIA_GROUPS = "ServiceCritGroups";
  public static final String VIEW_SERVICE_CRITERIA = "ServiceCriteria";

  public static final String VIEW_SERVICE_DISTINCT_CRITERIA = "ServiceDistinctCriteria";
  public static final String VIEW_SERVICE_DISTINCT_VALUES = "ServiceDistinctCritValues";

  public static final String VIEW_SERVICE_OBJECT_CRITERIA = "ServiceObjectCriteria";

  public static final String VIEW_SERVICE_FILES = "ServiceFiles";
  public static final String VIEW_SERVICE_DATES = "ServiceDates";

  public static final String VIEW_MAINTENANCE = "Maintenance";
  public static final String VIEW_SERVICE_INVOICES = "ServiceInvoices";
  public static final String VIEW_SERVICE_DEFECTS = "ServiceDefects";
  public static final String VIEW_SERVICE_DEFECT_ITEMS = "ServiceDefectItems";

  public static final String VIEW_SERVICE_SETTINGS = "ServiceSettings";

  public static final String COL_SERVICE_CATEGORY = "Category";
  public static final String COL_SERVICE_ADDRESS = "Address";
  public static final String COL_SERVICE_CUSTOMER = "Customer";
  public static final String COL_SERVICE_CONTRACTOR = "Contractor";

  public static final String COL_SERVICE_CRITERIA_GROUP = "Group";
  public static final String COL_SERVICE_CRITERIA_GROUP_NAME = "Name";
  public static final String COL_SERVICE_CRITERION_NAME = "Criterion";
  public static final String COL_SERVICE_CRITERION_VALUE = "Value";
  public static final String COL_SERVICE_CRITERIA_ORDINAL = "Ordinal";

  public static final String COL_SERVICE_OBJECT = "ServiceObject";

  public static final String COL_SERVICE_CATEGORY_NAME = "Name";

  public static final String COL_OBJECT_STATUS = "Status";
  public static final String COL_OBJECT_NAME = "Name";

  public static final String COL_MAINTENANCE_DATE = "Date";
  public static final String COL_MAINTENANCE_ITEM = "Item";
  public static final String COL_MAINTENANCE_INVOICE = "Invoice";
  public static final String COL_MAINTENANCE_DEFECT = "Defect";
  public static final String COL_MAINTENANCE_NOTES = "Notes";

  public static final String COL_SERVICE_DATE_FROM = "DateFrom";
  public static final String COL_SERVICE_DATE_UNTIL = "DateUntil";
  public static final String COL_SERVICE_DATE_COLOR = "Color";
  public static final String COL_SERVICE_DATE_NOTE = "Note";

  public static final String COL_SERVICE_CALENDAR_TASK_TYPES = "CalendarTaskTypes";
  public static final String COL_SERVICE_CALENDAR_MIN_DATE = "CalendarMinDate";
  public static final String COL_SERVICE_CALENDAR_MAX_DATE = "CalendarMaxDate";

  public static final String COL_DEFECT_SUPPLIER = "Supplier";
  public static final String COL_DEFECT_MANAGER = "Manager";

  public static final String COL_DEFECT = "Defect";
  public static final String COL_DEFECT_ITEM = "Item";
  public static final String COL_DEFECT_NOTE = "Note";

  public static final String ALS_SERVICE_CATEGORY_NAME = "CategoryName";
  public static final String ALS_SERVICE_CUSTOMER_NAME = "CustomerName";
  public static final String ALS_SERVICE_CONTRACTOR_NAME = "ContractorName";
  public static final String ALS_SERVICE_CUSTOMER_TYPE_NAME = "CustomerTypeName";

  public static final String ALS_MAINTENANCE_ITEM_NAME = "ItemName";

  public static final String ALS_DEFECT_SUPPLIER_NAME = "SupplierName";
  public static final String ALS_DEFECT_ITEM_NAME = "ItemName";
  public static final String ALS_DEFECT_UNIT_NAME = "UnitName";

  public static final String GRID_OBJECT_INVOICES = "ObjectInvoices";
  public static final String GRID_OBJECT_DEFECTS = "ObjectDefects";
  public static final String GRID_SERVICE_INVOICES = "ServiceInvoices";
  public static final String GRID_SVC_PROJECT_INVOICES = "SvcProjectInvoices";

  public static final String GRID_SERVICE_OBJECTS = "ServiceObjects";
  public static final String GRID_SERVICE_DEFECTS = "ServiceDefects";
  public static final String GRID_CHILD_SERVICE_OBJECTS = "ChildServiceObjects";

  public static final String PROP_MAIN_ITEM = "MainItem";
  public static final String PROP_CRITERIA = "Criteria";

  public static void register() {
    EnumUtils.register(ServiceCompanyKind.class);
    EnumUtils.register(SvcObjectStatus.class);
  }

  private ServiceConstants() {
  }
}
