package com.butent.bee.shared.modules.transport;

import com.google.common.collect.Lists;

import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collections;
import java.util.List;

public final class TransportConstants {

  public enum AssessmentStatus implements HasCaption {
    NEW(Localized.getConstants().trAssessmentStatusNew(), Localized.getConstants()
        .trAssessmentReturnToRequestQuestion(),
        OrderStatus.REQUEST, true),
    ANSWERED(Localized.getConstants().trAssessmentStatusAnswered(), Localized.getConstants()
        .trAssessmentMarkAsAnsweredQuestion(),
        null, false),
    LOST(Localized.getConstants().trAssessmentStatusLost(), Localized.getConstants()
        .trAssessmentMarkAsLostQuestion(),
        OrderStatus.CANCELED, false),
    ACTIVE(Localized.getConstants().trAssessmentStatusActive(), Localized.getConstants()
        .trAssessmentReturnToOrderQuestion(),
        OrderStatus.ACTIVE, true),
    CANCELED(Localized.getConstants().trAssessmentStatusCanceled(), Localized.getConstants()
        .trAssessmentCancelOrderQuestion(),
        OrderStatus.CANCELED, false),
    COMPLETED(Localized.getConstants().trAssessmentStatusCompleted(), Localized.getConstants()
        .trAssessmentMarkAsCompletedQuestion(),
        OrderStatus.COMPLETED, false);

    public static boolean in(int status, AssessmentStatus... statuses) {
      for (AssessmentStatus ts : statuses) {
        if (ts.ordinal() == status) {
          return true;
        }
      }
      return false;
    }

    private final String caption;
    private final String confirmation;
    private final OrderStatus orderStatus;
    private final boolean closable;

    private AssessmentStatus(String caption, String confirmation, OrderStatus orderStatus,
        boolean closable) {
      this.caption = caption;
      this.confirmation = confirmation;
      this.orderStatus = orderStatus;
      this.closable = closable;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public String getConfirmation() {
      return confirmation;
    }

    public OrderStatus getOrderStatus() {
      return orderStatus;
    }

    public boolean is(Integer status) {
      return status != null && ordinal() == status;
    }

    public boolean isClosable() {
      return closable;
    }
  }

  public enum TranspRegStatus implements HasCaption {
    NEW(Localized.getConstants().trRegistrationStatusNew()),
    CONFIRMED(Localized.getConstants().trRegistrationStatusConfirmed()),
    REJECTED(Localized.getConstants().trRegistrationStatusRejected());

    private final String caption;

    private TranspRegStatus(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public enum CargoRequestStatus implements HasCaption {
    NEW(Localized.getConstants().trRequestStatusNew()),
    ACTIVE(Localized.getConstants().trRequestStatusActive()),
    REJECTED(Localized.getConstants().trRequestStatusRejected()),
    FINISHED(Localized.getConstants().trRequestStatusFinished());

    private final String caption;

    private CargoRequestStatus(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public enum FuelSeason implements HasLocalizedCaption {
    SUMMER {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.summer();
      }
    },
    WINTER {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.winter();
      }
    };

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }
  }

  public enum ImportType implements HasLocalizedCaption {
    COSTS {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.trImportCosts();
      }

      @Override
      protected void init() {
        LocalizableConstants locale = Localized.getConstants();
        properties.add(new ImportProperty(VAR_IMPORT_ROW, locale.startRow()));
        properties.add(new ImportProperty(COL_COSTS_DATE, locale.date()));
        properties.add(new ImportProperty(COL_COSTS_ITEM, locale.itemOrService(),
            CommonsConstants.TBL_ITEMS, CommonsConstants.COL_ITEM_NAME));
        properties.add(new ImportProperty(COL_COSTS_QUANTITY, locale.quantity()));
        properties.add(new ImportProperty(COL_COSTS_PRICE, locale.price()));
        properties.add(new ImportProperty(COL_COSTS_CURRENCY, locale.currency(),
            ExchangeUtils.TBL_CURRENCIES, ExchangeUtils.COL_CURRENCY_NAME));
        properties.add(new ImportProperty(COL_COSTS_VAT, locale.vat()));
        properties.add(new ImportProperty(COL_COSTS_SUPPLIER, locale.supplier(),
            CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_COMPANY_NAME));
        properties.add(new ImportProperty(COL_NUMBER, locale.number()));
        properties.add(new ImportProperty(COL_VEHICLE, locale.trVehicle(),
            TBL_VEHICLES, COL_VEHICLE_NUMBER));
        properties.add(new ImportProperty(COL_COSTS_COUNTRY, locale.country(),
            CommonsConstants.TBL_COUNTRIES, CommonsConstants.COL_COUNTRY_NAME));
        properties.add(new ImportProperty(COL_COSTS_NOTE, locale.notes()));
      }
    },
    INVOICES {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.trImportInvoices();
      }

      @Override
      protected void init() {
      }
    };

    public static class ImportProperty {
      private final String name;
      private final String caption;
      private String relTable;
      private String relField;

      public ImportProperty(String name, String caption) {
        Assert.notEmpty(name);
        this.name = name;
        this.caption = BeeUtils.notEmpty(caption, name);
      }

      public ImportProperty(String name, String caption, String relTable, String relField) {
        this(name, caption);
        this.relTable = relTable;
        this.relField = relField;
      }

      public String getCaption() {
        return caption;
      }

      public String getName() {
        return name;
      }

      public String getRelField() {
        return relField;
      }

      public String getRelTable() {
        return relTable;
      }
    }

    final List<ImportProperty> properties = Lists.newArrayList();

    private ImportType() {
      init();
    }

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }

    public String[] getCaptions() {
      String[] props = new String[properties.size()];

      for (int i = 0; i < props.length; i++) {
        props[i] = properties.get(i).getCaption();
      }
      return props;
    }

    public List<ImportProperty> getProperties() {
      return Collections.unmodifiableList(properties);
    }

    public ImportProperty getProperty(Integer idx) {
      if (BeeUtils.isIndex(properties, idx)) {
        return properties.get(idx);
      }
      return null;
    }

    public ImportProperty getProperty(String name) {
      for (ImportProperty prop : properties) {
        if (BeeUtils.same(prop.getName(), name)) {
          return prop;
        }
      }
      return null;
    }

    protected abstract void init();
  }

  public enum OrderStatus implements HasLocalizedCaption {
    NEW {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.trOrderStatusNew();
      }
    },
    ACTIVE {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.trOrderStatusActive();
      }
    },
    CANCELED {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.trOrderStatusCanceled();
      }
    },
    COMPLETED {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.trOrderStatusCompleted();
      }
    },
    REQUEST {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.trOrderStatusRequest();
      }
    };

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }
  }

  public enum TripStatus implements HasCaption {
    NEW(Localized.getConstants().trTripStatusNew(), true),
    ACTIVE(Localized.getConstants().trTripStatusActive(), true),
    CANCELED(Localized.getConstants().trTripStatusCanceled(), false),
    COMPLETED(Localized.getConstants().trTripStatusCompleted(), false);

    private final String caption;
    private final boolean editable;

    private TripStatus(String caption, boolean editable) {
      this.caption = caption;
      this.editable = editable;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    public boolean isEditable() {
      return editable;
    }
  }

  public enum VehicleType {
    TRUCK {
      @Override
      public String getTripVehicleIdColumnName() {
        return COL_VEHICLE;
      }

      @Override
      public String getTripVehicleNumberColumnName() {
        return COL_VEHICLE + COL_VEHICLE_NUMBER;
      }
    },
    TRAILER {
      @Override
      public String getTripVehicleIdColumnName() {
        return COL_TRAILER;
      }

      @Override
      public String getTripVehicleNumberColumnName() {
        return COL_TRAILER + COL_VEHICLE_NUMBER;
      }
    };

    public abstract String getTripVehicleIdColumnName();

    public abstract String getTripVehicleNumberColumnName();
  }

  public static void register() {
    EnumUtils.register(AssessmentStatus.class);
    EnumUtils.register(TripStatus.class);
    EnumUtils.register(OrderStatus.class);

    EnumUtils.register(TranspRegStatus.class);
    EnumUtils.register(CargoRequestStatus.class);

    EnumUtils.register(FuelSeason.class);
    EnumUtils.register(ImportType.class);
  }

  public static final String TRANSPORT_MODULE = "Transport";
  public static final String TRANSPORT_METHOD = TRANSPORT_MODULE + "Method";

  public static final String SVC_GET_BEFORE = "GetBeforeData";
  public static final String SVC_GET_UNASSIGNED_CARGOS = "GetUnassignedCargos";
  public static final String SVC_GET_PROFIT = "GetProfit";
  public static final String SVC_GET_FX_DATA = "GetFxData";
  public static final String SVC_GET_SS_DATA = "GetSsData";
  public static final String SVC_GET_DTB_DATA = "GetDtbData";
  public static final String SVC_GET_TRUCK_TB_DATA = "GetTruckTbData";
  public static final String SVC_GET_TRAILER_TB_DATA = "GetTrailerTbData";
  public static final String SVC_GET_COLORS = "GetColors";
  public static final String SVC_GET_CARGO_USAGE = "GetCargoUsage";
  public static final String SVC_GET_ASSESSMENT_TOTALS = "GetAssessmentTotals";
  public static final String SVC_CREATE_INVOICE_ITEMS = "CreateInvoiceItems";
  public static final String SVC_GET_IMPORT_MAPPINGS = "GetImportMappings";
  public static final String SVC_DO_IMPORT = "DoImport";

  public static final String SVC_SEND_TO_ERP = "SendToERP";

  public static final String PRM_ERP_REFRESH_INTERVAL = "ERPRefreshIntervalInMinutes";

  public static final String VAR_TRIP_ID = Service.RPC_VAR_PREFIX + "trip_id";
  public static final String VAR_CARGO_ID = Service.RPC_VAR_PREFIX + "cargo_id";
  public static final String VAR_THEME_ID = Service.RPC_VAR_PREFIX + "theme_id";

  public static final String VAR_INCOME = "Income";
  public static final String VAR_EXPENSE = "Expense";
  public static final String VAR_TOTAL = "Total";

  public static final String VAR_MAPPING_TABLE = "MappingTable";
  public static final String VAR_MAPPING_FIELD = "MappingField";
  public static final String VAR_IMPORT_FILE = "File";
  public static final String VAR_IMPORT_ROW = "Row";

  public static final String TBL_TRANSPORT_GROUPS = "TransportGroups";

  public static final String TBL_VEHICLES = "Vehicles";
  public static final String TBL_VEHICLE_SERVICES = "VehicleServices";
  public static final String TBL_VEHICLE_GROUPS = "VehicleGroups";
  public static final String TBL_VEHICLE_SERVICE_TYPES = "ServiceTypes";
  public static final String TBL_EXPEDITION_TYPES = "ExpeditionTypes";

  public static final String TBL_TRIPS = "Trips";
  public static final String TBL_TRIP_DRIVERS = "TripDrivers";
  public static final String TBL_TRIP_COSTS = "TripCosts";
  public static final String TBL_TRIP_FUEL_COSTS = "TripFuelCosts";

  public static final String TBL_TRANSPORT_SETTINGS = "TransportSettings";

  public static final String TBL_ORDERS = "TransportationOrders";
  public static final String TBL_ORDER_CARGO = "OrderCargo";
  public static final String TBL_CARGO_TRIPS = "CargoTrips";
  public static final String TBL_CARGO_PLACES = "CargoPlaces";
  public static final String TBL_CARGO_INCOMES = "CargoIncomes";
  public static final String TBL_CARGO_EXPENSES = "CargoExpenses";
  public static final String TBL_CARGO_HANDLING = "CargoHandling";
  public static final String TBL_CARGO_ASSESSORS = "CargoAssessors";
  public static final String TBL_SERVICES = "Services";

  public static final String TBL_DRIVERS = "Drivers";
  public static final String TBL_DRIVER_GROUPS = "DriverGroups";
  public static final String TBL_DRIVER_ABSENCE = "DriverAbsence";
  public static final String TBL_ABSENCE_TYPES = "AbsenceTypes";

  public static final String TBL_FUEL_TYPES = "FuelTypes";

  public static final String TBL_REGISTRATIONS = "TranspRegistrations";

  public static final String TBL_IMPORT_OPTIONS = "ImportOptions";
  public static final String TBL_IMPORT_PROPERTIES = "ImportProperties";
  public static final String TBL_IMPORT_MAPPINGS = "ImportMappings";

  public static final String VIEW_ORDERS = "TransportationOrders";

  public static final String VIEW_ORDER_CARGO = "OrderCargo";
  public static final String VIEW_CARGO_TRIPS = "CargoTrips";
  public static final String VIEW_CARGO_HANDLING = "CargoHandling";

  public static final String VIEW_ALL_CARGO = "AllCargo";
  public static final String VIEW_WAITING_CARGO = "WaitingCargo";
  public static final String VIEW_CARGO_INVOICE_INCOMES = "CargoInvoiceIncomes";
  public static final String VIEW_CARGO_INVOICES = "CargoInvoices";
  public static final String VIEW_CARGO_CREDIT_INCOMES = "CargoCreditIncomes";
  public static final String VIEW_CARGO_CREDIT_INVOICES = "CargoCreditInvoices";

  public static final String VIEW_TRIPS = TBL_TRIPS;
  public static final String VIEW_EXPEDITION_TRIPS = "ExpeditionTrips";
  public static final String VIEW_ACTIVE_TRIPS = "ActiveTrips";

  public static final String VIEW_TRIP_CARGO = "TripCargo";
  public static final String VIEW_TRIP_DRIVERS = "TripDrivers";
  public static final String VIEW_TRIP_ROUTES = "TripRoutes";
  public static final String VIEW_TRIP_COSTS = "TripCosts";
  public static final String VIEW_TRIP_FUEL_COSTS = "TripFuelCosts";
  public static final String VIEW_TRIP_FUEL_CONSUMPTIONS = "TripFuelConsumptions";

  public static final String VIEW_VEHICLES = "Vehicles";
  public static final String VIEW_VEHICLE_SERVICES = "VehicleServices";
  public static final String VIEW_FUEL_CONSUMPTIONS = "FuelConsumptions";
  public static final String VIEW_FUEL_TEMPERATURES = "FuelTemperatures";

  public static final String VIEW_SPARE_PARTS = "SpareParts";

  public static final String VIEW_TRANSPORT_SETTINGS = "TransportSettings";

  public static final String VIEW_DRIVERS = "Drivers";
  public static final String VIEW_DRIVER_ABSENCE = "DriverAbsence";
  public static final String VIEW_ABSENCE_TYPES = "AbsenceTypes";

  public static final String VIEW_ASSESSMENT_FORWARDERS = "AssessmentForwarders";
  public static final String VIEW_ASSESSMENT_TRANSPORTATIONS = "AssessmentTransportations";

  public static final String VIEW_REGISTRATIONS = "TranspRegistrations";
  public static final String VIEW_SHIPMENT_REQUESTS = "ShipmentRequests";
  public static final String VIEW_CARGO_REQUESTS = "CargoRequests";
  public static final String VIEW_CARGO_REQUEST_TEMPLATES = "CargoReqTemplates";
  public static final String VIEW_CARGO_REQUEST_FILES = "CargoRequestFiles";

  public static final String VIEW_EXPEDITION_TYPES = "ExpeditionTypes";
  public static final String VIEW_SHIPPING_TERMS = "ShippingTerms";

  public static final String COL_GROUP = "Group";

  public static final String COL_TRIP = "Trip";
  public static final String COL_TRIP_ID = "TripID";
  public static final String COL_TRIP_NO = "TripNo";
  public static final String COL_TRIP_DATE = "Date";
  public static final String COL_TRIP_DATE_FROM = "DateFrom";
  public static final String COL_TRIP_DATE_TO = "DateTo";
  public static final String COL_TRIP_PLANNED_END_DATE = "PlannedEndDate";
  public static final String COL_TRIP_PERCENT = "TripPercent";
  public static final String COL_TRIP_NOTES = "Notes";
  public static final String COL_TRIP_STATUS = "Status";

  public static final String COL_EXPEDITION = "Expedition";
  public static final String COL_FORWARDER = "Forwarder";
  public static final String COL_FORWARDER_VEHICLE = "ForwarderVehicle";

  public static final String COL_CARGO = "Cargo";
  public static final String COL_CARGO_DESCRIPTION = "Description";
  public static final String COL_CARGO_ID = "CargoID";
  public static final String COL_CARGO_PERCENT = "CargoPercent";
  public static final String COL_CARGO_TRIP_ID = "CargoTripID";
  public static final String COL_CARGO_CMR = "Cmr";
  public static final String COL_CARGO_NOTES = "Notes";
  public static final String COL_CARGO_SHIPPING_TERM = "ShippingTerm";
  public static final String COL_CARGO_QUANTITY = "Quantity";
  public static final String COL_CARGO_WEIGHT = "Weight";
  public static final String COL_CARGO_VOLUME = "Volume";
  public static final String COL_CARGO_LDM = "LDM";
  public static final String COL_CARGO_LENGTH = "Length";
  public static final String COL_CARGO_WIDTH = "Width";
  public static final String COL_CARGO_HEIGHT = "Height";
  public static final String COL_CARGO_PALETTES = "Palettes";
  public static final String COL_CARGO_VALUE = "Value";
  public static final String COL_CARGO_VALUE_CURRENCY = "ValueCurrency";

  public static final String COL_CARGO_HANDLING_NOTES = "Notes";

  public static final String COL_ASSESSOR = "Assessor";
  public static final String COL_ASSESSOR_DATE = "Date";
  public static final String COL_ASSESSOR_MANAGER = "Manager";
  public static final String COL_ASSESSOR_NOTES = "Notes";

  public static final String COL_STATUS = "Status";
  public static final String COL_OWNER = "Owner";
  public static final String COL_OWNER_NAME = "OwnerName";
  public static final String COL_MODEL = "Model";
  public static final String COL_PARENT_MODEL_NAME = "ParentModelName";
  public static final String COL_MODEL_NAME = "ModelName";
  public static final String COL_NUMBER = "Number";
  public static final String COL_TYPE_NAME = "TypeName";

  public static final String COL_COSTS_DATE = "Date";
  public static final String COL_COSTS_ITEM = "Item";
  public static final String COL_COSTS_QUANTITY = "Quantity";
  public static final String COL_COSTS_PRICE = "Price";
  public static final String COL_COSTS_CURRENCY = "Currency";
  public static final String COL_COSTS_VAT = "Vat";
  public static final String COL_COSTS_COUNTRY = "Country";
  public static final String COL_COSTS_SUPPLIER = "Supplier";
  public static final String COL_COSTS_NOTE = "Note";

  public static final String COL_ORDER = "Order";
  public static final String COL_ORDER_NO = "OrderNo";
  public static final String COL_ORDER_DATE = "Date";
  public static final String COL_CUSTOMER = "Customer";
  public static final String COL_CUSTOMER_NAME = "CustomerName";
  public static final String COL_PAYER = "Payer";
  public static final String COL_PAYER_NAME = "PayerName";

  public static final String COL_SERVICE = "Service";
  public static final String COL_DATE = "Date";
  public static final String COL_AMOUNT = "Amount";

  public static final String COL_DESCRIPTION = "Description";

  public static final String COL_UNLOADING_PLACE = "UnloadingPlace";
  public static final String COL_LOADING_PLACE = "LoadingPlace";

  public static final String COL_PLACE_DATE = "Date";
  public static final String COL_PLACE_CONTACT = "Contact";
  public static final String COL_PLACE_CITY = "City";
  public static final String COL_PLACE_COUNTRY = "Country";
  public static final String COL_PLACE_ADDRESS = "Address";
  public static final String COL_PLACE_POST_INDEX = "PostIndex";
  public static final String COL_PLACE_TERMINAL = "Terminal";
  public static final String COL_PLACE_PHONE = "Phone";
  public static final String COL_PLACE_FAX = "Fax";
  public static final String COL_PLACE_NUMBER = "Number";

  public static final String COL_VEHICLE_ID = "VehicleID";
  public static final String COL_VEHICLE = "Vehicle";
  public static final String COL_TRAILER = "Trailer";
  public static final String COL_VEHICLE_NUMBER = "Number";
  public static final String COL_FUEL = "Fuel";

  public static final String COL_VEHICLE_START_DATE = "StartDate";
  public static final String COL_VEHICLE_END_DATE = "EndDate";
  public static final String COL_VEHICLE_NOTES = "Notes";

  public static final String COL_DRIVER = "Driver";
  public static final String COL_DRIVER_PERSON = "CompanyPerson";
  public static final String COL_DRIVER_START_DATE = "StartDate";
  public static final String COL_DRIVER_END_DATE = "EndDate";
  public static final String COL_DRIVER_EXPERIENCE = "Experience";
  public static final String COL_DRIVER_NOTES = "Notes";

  public static final String COL_TRIP_DRIVER_FROM = "DateFrom";
  public static final String COL_TRIP_DRIVER_TO = "DateTo";
  public static final String COL_TRIP_DRIVER_NOTE = "Note";

  public static final String COL_VEHICLE_SERVICE_DATE = "Date";
  public static final String COL_VEHICLE_SERVICE_DATE_TO = "DateTo";
  public static final String COL_VEHICLE_SERVICE_TYPE = "Type";
  public static final String COL_VEHICLE_SERVICE_NAME = "Name";
  public static final String COL_VEHICLE_SERVICE_NOTES = "Notes";

  public static final String COL_ABSENCE_NAME = "Name";
  public static final String COL_ABSENCE_LABEL = "Label";
  public static final String COL_ABSENCE_COLOR = "Color";

  public static final String COL_ABSENCE = "Absence";
  public static final String COL_ABSENCE_FROM = "DateFrom";
  public static final String COL_ABSENCE_TO = "DateTo";
  public static final String COL_ABSENCE_NOTES = "Notes";

  public static final String COL_IS_TRUCK = "IsTruck";
  public static final String COL_IS_TRAILER = "IsTrailer";

  public static final String COL_FX_PIXELS_PER_CUSTOMER = "FxPixelsPerCustomer";
  public static final String COL_FX_PIXELS_PER_ORDER = "FxPixelsPerOrder";

  public static final String COL_FX_COUNTRY_FLAGS = "FxCountryFlags";
  public static final String COL_FX_PLACE_INFO = "FxPlaceInfo";

  public static final String COL_FX_PIXELS_PER_DAY = "FxPixelsPerDay";
  public static final String COL_FX_PIXELS_PER_ROW = "FxPixelsPerRow";

  public static final String COL_FX_HEADER_HEIGHT = "FxHeaderHeight";
  public static final String COL_FX_FOOTER_HEIGHT = "FxFooterHeight";

  public static final String COL_FX_THEME = "FxTheme";

  public static final String COL_FX_ITEM_OPACITY = "FxItemOpacity";
  public static final String COL_FX_STRIP_OPACITY = "FxStripOpacity";

  public static final String COL_SS_PIXELS_PER_TRUCK = "SsPixelsPerTruck";
  public static final String COL_SS_PIXELS_PER_TRIP = "SsPixelsPerTrip";

  public static final String COL_SS_SEPARATE_TRIPS = "SsSeparateTrips";
  public static final String COL_SS_SEPARATE_CARGO = "SsSeparateCargo";

  public static final String COL_SS_COUNTRY_FLAGS = "SsCountryFlags";
  public static final String COL_SS_PLACE_INFO = "SsPlaceInfo";

  public static final String COL_SS_PIXELS_PER_DAY = "SsPixelsPerDay";
  public static final String COL_SS_PIXELS_PER_ROW = "SsPixelsPerRow";

  public static final String COL_SS_HEADER_HEIGHT = "SsHeaderHeight";
  public static final String COL_SS_FOOTER_HEIGHT = "SsFooterHeight";

  public static final String COL_SS_THEME = "SsTheme";

  public static final String COL_SS_ITEM_OPACITY = "SsItemOpacity";
  public static final String COL_SS_STRIP_OPACITY = "SsStripOpacity";

  public static final String COL_DTB_PIXELS_PER_DRIVER = "DtbPixelsPerDriver";

  public static final String COL_DTB_COUNTRY_FLAGS = "DtbCountryFlags";
  public static final String COL_DTB_PLACE_INFO = "DtbPlaceInfo";

  public static final String COL_DTB_PIXELS_PER_DAY = "DtbPixelsPerDay";
  public static final String COL_DTB_PIXELS_PER_ROW = "DtbPixelsPerRow";

  public static final String COL_DTB_HEADER_HEIGHT = "DtbHeaderHeight";
  public static final String COL_DTB_FOOTER_HEIGHT = "DtbFooterHeight";

  public static final String COL_DTB_COLOR = "DtbColor";

  public static final String COL_DTB_ITEM_OPACITY = "DtbItemOpacity";
  public static final String COL_DTB_STRIP_OPACITY = "DtbStripOpacity";

  public static final String COL_TRUCK_PIXELS_PER_NUMBER = "TruckPixelsPerNumber";
  public static final String COL_TRUCK_PIXELS_PER_INFO = "TruckPixelsPerInfo";

  public static final String COL_TRUCK_SEPARATE_CARGO = "TruckSeparateCargo";
  public static final String COL_TRUCK_COUNTRY_FLAGS = "TruckCountryFlags";
  public static final String COL_TRUCK_PLACE_INFO = "TruckPlaceInfo";

  public static final String COL_TRUCK_PIXELS_PER_DAY = "TruckPixelsPerDay";
  public static final String COL_TRUCK_PIXELS_PER_ROW = "TruckPixelsPerRow";

  public static final String COL_TRUCK_HEADER_HEIGHT = "TruckHeaderHeight";
  public static final String COL_TRUCK_FOOTER_HEIGHT = "TruckFooterHeight";

  public static final String COL_TRUCK_THEME = "TruckTheme";

  public static final String COL_TRUCK_ITEM_OPACITY = "TruckItemOpacity";
  public static final String COL_TRUCK_STRIP_OPACITY = "TruckStripOpacity";

  public static final String COL_TRAILER_PIXELS_PER_NUMBER = "TrailerPixelsPerNumber";
  public static final String COL_TRAILER_PIXELS_PER_INFO = "TrailerPixelsPerInfo";

  public static final String COL_TRAILER_SEPARATE_CARGO = "TrailerSeparateCargo";
  public static final String COL_TRAILER_COUNTRY_FLAGS = "TrailerCountryFlags";
  public static final String COL_TRAILER_PLACE_INFO = "TrailerPlaceInfo";

  public static final String COL_TRAILER_PIXELS_PER_DAY = "TrailerPixelsPerDay";
  public static final String COL_TRAILER_PIXELS_PER_ROW = "TrailerPixelsPerRow";

  public static final String COL_TRAILER_HEADER_HEIGHT = "TrailerHeaderHeight";
  public static final String COL_TRAILER_FOOTER_HEIGHT = "TrailerFooterHeight";

  public static final String COL_TRAILER_THEME = "TrailerTheme";

  public static final String COL_TRAILER_ITEM_OPACITY = "TrailerItemOpacity";
  public static final String COL_TRAILER_STRIP_OPACITY = "TrailerStripOpacity";

  public static final String COL_CARGO_REQUEST_STATUS = "Status";

  public static final String COL_CARGO_REQUEST_TEMPLATE_NAME = "Name";

  public static final String COL_REGISTRATION_DATE = "Date";
  public static final String COL_REGISTRATION_STATUS = "Status";

  public static final String COL_REGISTRATION_COMPANY_NAME = "CompanyName";
  public static final String COL_REGISTRATION_COMPANY_CODE = "CompanyCode";
  public static final String COL_REGISTRATION_VAT_CODE = "VatCode";
  public static final String COL_REGISTRATION_CONTACT = "Contact";
  public static final String COL_REGISTRATION_CONTACT_POSITION = "ContactPosition";

  public static final String COL_REGISTRATION_ADDRESS = "Address";
  public static final String COL_REGISTRATION_CITY = "City";
  public static final String COL_REGISTRATION_COUNTRY = "Country";

  public static final String COL_REGISTRATION_PHONE = "Phone";
  public static final String COL_REGISTRATION_MOBILE = "Mobile";

  public static final String COL_REGISTRATION_FAX = "Fax";
  public static final String COL_REGISTRATION_EMAIL = "Email";

  public static final String COL_REGISTRATION_EXCHANGE_CODE = "ExchangeCode";

  public static final String COL_REGISTRATION_BANK = "Bank";
  public static final String COL_REGISTRATION_BANK_ADDRESS = "BankAddress";
  public static final String COL_REGISTRATION_BANK_ACCOUNT = "BankAccount";
  public static final String COL_REGISTRATION_SWIFT = "Swift";

  public static final String COL_REGISTRATION_NOTES = "Notes";
  public static final String COL_REGISTRATION_HOST = "Host";
  public static final String COL_REGISTRATION_AGENT = "Agent";

  public static final String COL_QUERY_DATE = "Date";
  public static final String COL_QUERY_STATUS = "Status";
  public static final String COL_QUERY_CUSTOMER_NAME = "CustomerName";
  public static final String COL_QUERY_CUSTOMER_CODE = "CustomerCode";
  public static final String COL_QUERY_CUSTOMER_VAT_CODE = "CustomerVatCode";
  public static final String COL_QUERY_CUSTOMER_ADDRESS = "CustomerAddress";
  public static final String COL_QUERY_CUSTOMER_PHONE = "CustomerPhone";
  public static final String COL_QUERY_CUSTOMER_EMAIL = "CustomerEmail";
  public static final String COL_QUERY_CUSTOMER_CONTACT = "CustomerContact";
  public static final String COL_QUERY_CUSTOMER_CONTACT_POSITION = "CustomerContactPosition";
  public static final String COL_QUERY_CUSTOMER_EXCHANGE_CODE = "CustomerExchangeCode";
  public static final String COL_QUERY_LOADING_COMPANY_NAME = "LoadingCompanyName";
  public static final String COL_QUERY_LOADING_EMAIL = "LoadingEmail";
  public static final String COL_QUERY_LOADING_CITY = "LoadingCity";
  public static final String COL_QUERY_UNLOADING_COMPANY_NAME = "UnloadingCompanyName";
  public static final String COL_QUERY_UNLOADING_EMAIL = "UnloadingEmail";
  public static final String COL_QUERY_UNLOADING_CITY = "UnloadingCity";
  public static final String COL_QUERY_EXPEDITION = "Expedition";
  public static final String COL_QUERY_DELIVERY_DATE = "DeliveryDate";
  public static final String COL_QUERY_DELIVERY_TIME = "DeliveryTime";
  public static final String COL_QUERY_TERMS_OF_DELIVERY = "TermsOfDelivery";
  public static final String COL_QUERY_CUSTOMS_BROKERAGE = "CustomsBrokerage";
  public static final String COL_QUERY_FREIGHT_INSURANCE = "FreightInsurance";
  public static final String COL_QUERY_CARGO = "Cargo";
  public static final String COL_QUERY_MANAGER = "Manager";
  public static final String COL_QUERY_NOTES = "Notes";
  public static final String COL_QUERY_HOST = "Host";
  public static final String COL_QUERY_AGENT = "Agent";

  public static final String COL_EXPEDITION_TYPE_NAME = "Name";
  public static final String COL_EXPEDITION_TYPE_SELF_SERVICE = "SelfService";

  public static final String COL_SHIPPING_TERM_NAME = "Name";
  public static final String COL_SHIPPING_TERM_SELF_SERVICE = "SelfService";

  public static final String COL_CRF_REQUEST = "CargoRequest";
  public static final String COL_CRF_FILE = "File";
  public static final String COL_CRF_CAPTION = "Caption";

  public static final String COL_IMPORT_OPTION = "Option";
  public static final String COL_IMPORT_TYPE = "Type";
  public static final String COL_IMPORT_PROPERTY = "Property";
  public static final String COL_IMPORT_VALUE = "Value";
  public static final String COL_IMPORT_MAPPING = "Mapping";

  public static final String FORM_NEW_VEHICLE = "NewVehicle";
  public static final String FORM_ORDER = "TransportationOrder";
  public static final String FORM_TRIP = "Trip";
  public static final String FORM_EXPEDITION_TRIP = "ExpeditionTrip";
  public static final String FORM_CARGO = "OrderCargo";
  public static final String FORM_ASSESSMENT = "Assessment";
  public static final String FORM_ASSESSMENT_TRANSPORTATION = "AssessmentTransportation";

  public static final String FORM_NEW_CARGO_INVOICE = "NewCargoInvoice";
  public static final String FORM_NEW_CARGO_CREDIT_INVOICE = "NewCargoCreditInvoice";
  public static final String FORM_CARGO_INVOICE = "CargoInvoice";
  public static final String FORM_CARGO_CREDIT_INVOICE = "CargoCreditInvoice";

  public static final String FORM_FX_SETTINGS = "TrFxSettings";
  public static final String FORM_SS_SETTINGS = "TrSsSettings";
  public static final String FORM_DTB_SETTINGS = "TrDtbSettings";
  public static final String FORM_TRUCK_SETTINGS = "TruckTbSettings";
  public static final String FORM_TRAILER_SETTINGS = "TrailerTbSettings";

  public static final String FORM_REGISTRATION = "TranspRegistration";
  public static final String FORM_SHIPMENT_REQUEST = "ShipmentRequest";
  public static final String FORM_NEW_CARGO_REQUEST = "NewCargoRequest";
  public static final String FORM_CARGO_REQUEST = "CargoRequest";

  public static final String FORM_IMPORT_OPTION = "ImportOption";

  public static final String PROP_COLORS = "Colors";
  public static final String PROP_COUNTRIES = "Countries";
  public static final String PROP_DRIVERS = "Drivers";
  public static final String PROP_ABSENCE = "Absence";
  public static final String PROP_VEHICLES = "Vehicles";
  public static final String PROP_VEHICLE_SERVICES = "VehicleServices";
  public static final String PROP_ORDER_CARGO = "OrderCargo";
  public static final String PROP_TRIPS = "Trips";
  public static final String PROP_TRIP_DRIVERS = "TripDrivers";
  public static final String PROP_FREIGHTS = "Freights";
  public static final String PROP_CARGO_HANDLING = "CargoHandling";

  public static final String ALS_TRIP_DATE = "TripDate";
  public static final String ALS_ORDER_DATE = "OrderDate";

  public static final String ALS_VEHICLE_NUMBER = "VehicleNumber";
  public static final String ALS_TRAILER_NUMBER = "TrailerNumber";

  public static final String ALS_TRIP_VERSION = "TripVersion";
  public static final String ALS_CARGO_TRIP_VERSION = "CargoTripVersion";

  public static final String ALS_ABSENCE_NAME = "AbsenceName";
  public static final String ALS_ABSENCE_LABEL = "AbsenceLabel";

  public static final String ALS_CARGO_DESCRIPTION = "CargoDescription";

  public static final String ALS_REQUEST_CUSTOMER_COMPANY = "CustomerCompany";

  public static final String DATA_TYPE_ORDER_CARGO = "OrderCargo";
  public static final String DATA_TYPE_TRIP = "Trip";
  public static final String DATA_TYPE_FREIGHT = "Freight";
  public static final String DATA_TYPE_TRUCK = "Truck";
  public static final String DATA_TYPE_TRAILER = "Trailer";
  public static final String DATA_TYPE_DRIVER = "Driver";

  public static final String DEFAULT_CARGO_DESCRIPTION = "*";

  public static final String STYLE_SHEET = "transport";

  public static String defaultLoadingColumnAlias(String colName) {
    return "DefLoad" + colName;
  }

  public static String defaultUnloadingColumnAlias(String colName) {
    return "DefUnload" + colName;
  }

  public static String loadingColumnAlias(String colName) {
    return "Loading" + colName;
  }

  public static String unloadingColumnAlias(String colName) {
    return "Unloading" + colName;
  }

  private TransportConstants() {
  }
}
