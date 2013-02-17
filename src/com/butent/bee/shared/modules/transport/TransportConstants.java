package com.butent.bee.shared.modules.transport;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.ui.HasCaption;

public class TransportConstants {

  public enum OrderStatus implements HasCaption {
    CREATED("Naujas"),
    ACTIVATED("Aktyvus"),
    CONFIRMED("Patvirtintas"),
    CANCELED("Atšauktas"),
    COMPLETED("Baigtas");

    private final String caption;

    private OrderStatus(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }

  public static final String TRANSPORT_MODULE = "Transport";
  public static final String TRANSPORT_METHOD = TRANSPORT_MODULE + "Method";

  public static final String SVC_GET_BEFORE = "GetBeforeData";
  public static final String SVC_GET_PROFIT = "GetProfit";
  public static final String SVC_GET_FX_DATA = "GetFxData";
  public static final String SVC_GET_SS_DATA = "GetSsData";
  public static final String SVC_GET_DTB_DATA = "GetDtbData";
  public static final String SVC_GET_COLORS = "GetColors";
  public static final String SVC_GET_ORDER_TRIPS = "GetOrderTrips";
  public static final String SVC_GET_CARGO_USAGE = "GetCargoUsage";

  public static final String VAR_TRIP_ID = Service.RPC_VAR_PREFIX + "trip_id";
  public static final String VAR_CARGO_ID = Service.RPC_VAR_PREFIX + "cargo_id";
  public static final String VAR_THEME_ID = Service.RPC_VAR_PREFIX + "theme_id";

  public static final String TBL_VEHICLES = "Vehicles";
  public static final String TBL_VEHICLE_SERVICES = "VehicleServices";
  public static final String TBL_SERVICE_TYPES = "ServiceTypes";
  public static final String TBL_EXPEDITION_TYPES = "ExpeditionTypes";

  public static final String TBL_TRIPS = "Trips";
  public static final String TBL_TRIP_DRIVERS = "TripDrivers";

  public static final String TBL_TRANSPORT_SETTINGS = "TransportSettings";

  public static final String TBL_ORDERS = "TransportationOrders";
  public static final String TBL_ORDER_CARGO = "OrderCargo";
  public static final String TBL_CARGO_TRIPS = "CargoTrips";
  public static final String TBL_CARGO_PLACES = "CargoPlaces";

  public static final String TBL_DRIVERS = "Drivers";
  public static final String TBL_DRIVER_ABSENCE = "DriverAbsence";
  public static final String TBL_ABSENCE_TYPES = "AbsenceTypes";
  
  public static final String VIEW_ORDERS = "TransportationOrders";

  public static final String VIEW_CARGO = "OrderCargo";
  public static final String VIEW_CARGO_SERVICES = "CargoServices";
  public static final String VIEW_CARGO_TRIPS = "CargoTrips";
  public static final String VIEW_CARGO_HANDLING = "CargoHandling";
  public static final String VIEW_CARGO_LIST = "CargoList";

  public static final String VIEW_TRIPS = TBL_TRIPS;
  public static final String VIEW_EXPEDITION_TRIPS = "ExpeditionTrips";
  public static final String VIEW_ALL_TRIPS = "AllTrips";

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
  
  public static final String COL_TRIP = "Trip";
  public static final String COL_TRIP_ID = "TripID";
  public static final String COL_TRIP_NO = "TripNo";
  public static final String COL_TRIP_DATE = "Date";
  public static final String COL_TRIP_DATE_FROM = "DateFrom";
  public static final String COL_TRIP_DATE_TO = "DateTo";
  public static final String COL_TRIP_PLANNED_END_DATE = "PlannedEndDate";
  public static final String COL_TRIP_PERCENT = "TripPercent";

  public static final String COL_EXPEDITION = "Expedition";
  public static final String COL_EXPEDITION_TYPE = "Name";
  public static final String COL_FORWARDER = "Forwarder";
  public static final String COL_FORWARDER_VEHICLE = "ForwarderVehicle";

  public static final String COL_CARGO = "Cargo";
  public static final String COL_CARGO_ID = "CargoID";
  public static final String COL_CARGO_PERCENT = "CargoPercent";
  public static final String COL_CARGO_TRIP_ID = "CargoTripID";

  public static final String COL_STATUS = "Status";
  public static final String COL_OWNER = "Owner";
  public static final String COL_OWNER_NAME = "OwnerName";
  public static final String COL_PARENT_MODEL_NAME = "ParentModelName";
  public static final String COL_MODEL_NAME = "ModelName";
  public static final String COL_NUMBER = "Number";

  public static final String COL_ORDER = "Order";
  public static final String COL_ORDER_NO = "OrderNo";
  public static final String COL_ORDER_DATE = "Date";
  public static final String COL_CUSTOMER = "Customer";
  public static final String COL_CUSTOMER_NAME = "CustomerName";

  public static final String COL_DESCRIPTION = "Description";

  public static final String COL_UNLOADING_PLACE = "UnloadingPlace";
  public static final String COL_LOADING_PLACE = "LoadingPlace";

  public static final String COL_PLACE_DATE = "Date";
  public static final String COL_COUNTRY = "Country";
  public static final String COL_PLACE = "Place";
  public static final String COL_TERMINAL = "Terminal";

  public static final String COL_PLACE_NAME = "PlaceName";

  public static final String COL_USER = "User";

  public static final String COL_VEHICLE_ID = "VehicleID";
  public static final String COL_VEHICLE = "Vehicle";
  public static final String COL_TRAILER = "Trailer";

  public static final String COL_VEHICLE_NUMBER = "VehicleNumber";
  public static final String COL_TRAILER_NUMBER = "TrailerNumber";

  public static final String COL_DRIVER = "Driver";
  public static final String COL_DRIVER_PERSON = "CompanyPerson";
  public static final String COL_DRIVER_START_DATE = "StartDate";
  public static final String COL_DRIVER_END_DATE = "EndDate";

  public static final String COL_TRIP_DRIVER_FROM = "DateFrom";
  public static final String COL_TRIP_DRIVER_TO = "DateTo";
  
  public static final String COL_SERVICE_DATE = "Date";
  public static final String COL_SERVICE_TYPE = "Type";
  public static final String COL_SERVICE_NAME = "Name";
  public static final String COL_SERVICE_NOTES = "Notes";

  public static final String COL_ABSENCE_NAME = "Name";
  public static final String COL_ABSENCE_LABEL = "Label";
  public static final String COL_ABSENCE_COLOR = "Color";

  public static final String COL_ABSENCE = "Absence";
  public static final String COL_ABSENCE_FROM = "DateFrom";
  public static final String COL_ABSENCE_TO = "DateTo";
  public static final String COL_ABSENCE_NOTES = "Notes";
  
  public static final String COL_FX_PIXELS_PER_CUSTOMER = "FxPixelsPerCustomer";
  public static final String COL_FX_PIXELS_PER_ORDER = "FxPixelsPerOrder";
  public static final String COL_FX_PIXELS_PER_DAY = "FxPixelsPerDay";
  public static final String COL_FX_PIXELS_PER_ROW = "FxPixelsPerRow";

  public static final String COL_FX_HEADER_HEIGHT = "FxHeaderHeight";
  public static final String COL_FX_FOOTER_HEIGHT = "FxFooterHeight";

  public static final String COL_FX_SLIDER_WIDTH = "FxSliderWidth";
  public static final String COL_FX_BAR_HEIGHT = "FxBarHeight";

  public static final String COL_FX_THEME = "FxTheme";

  public static final String COL_FX_ITEM_OPACITY = "FxItemOpacity";
  public static final String COL_FX_STRIP_OPACITY = "FxStripOpacity";

  public static final String COL_SS_PIXELS_PER_TRUCK = "SsPixelsPerTruck";
  public static final String COL_SS_PIXELS_PER_TRIP = "SsPixelsPerTrip";
  public static final String COL_SS_SEPARATE_TRIPS = "SsSeparateTrips";

  public static final String COL_SS_PIXELS_PER_DAY = "SsPixelsPerDay";
  public static final String COL_SS_PIXELS_PER_ROW = "SsPixelsPerRow";

  public static final String COL_SS_HEADER_HEIGHT = "SsHeaderHeight";
  public static final String COL_SS_FOOTER_HEIGHT = "SsFooterHeight";

  public static final String COL_SS_SLIDER_WIDTH = "SsSliderWidth";
  public static final String COL_SS_BAR_HEIGHT = "SsBarHeight";

  public static final String COL_SS_THEME = "SsTheme";

  public static final String COL_SS_ITEM_OPACITY = "SsItemOpacity";
  public static final String COL_SS_STRIP_OPACITY = "SsStripOpacity";

  public static final String COL_DTB_PIXELS_PER_DRIVER = "DtbPixelsPerDriver";

  public static final String COL_DTB_PIXELS_PER_DAY = "DtbPixelsPerDay";
  public static final String COL_DTB_PIXELS_PER_ROW = "DtbPixelsPerRow";

  public static final String COL_DTB_HEADER_HEIGHT = "DtbHeaderHeight";
  public static final String COL_DTB_FOOTER_HEIGHT = "DtbFooterHeight";

  public static final String COL_DTB_SLIDER_WIDTH = "DtbSliderWidth";
  public static final String COL_DTB_BAR_HEIGHT = "DtbBarHeight";

  public static final String COL_DTB_ITEM_OPACITY = "DtbItemOpacity";
  public static final String COL_DTB_STRIP_OPACITY = "DtbStripOpacity";
  
  public static final String FORM_NEW_VEHICLE = "NewVehicle";
  public static final String FORM_ORDER = "TransportationOrder";
  public static final String FORM_TRIP = "Trip";
  public static final String FORM_EXPEDITION_TRIP = "ExpeditionTrip";
  public static final String FORM_CARGO = "OrderCargo";

  public static final String FORM_FX_SETTINGS = "TrFxSettings";
  public static final String FORM_SS_SETTINGS = "TrSsSettings";
  public static final String FORM_DTB_SETTINGS = "TrDtbSettings";

  public static final String PROP_COLORS = "Colors";
  public static final String PROP_COUNTRIES = "Countries";
  public static final String PROP_DRIVERS = "Drivers";
  public static final String PROP_ABSENCE = "Absence";
  public static final String PROP_VEHICLE_SERVICES = "VehicleServices";
  public static final String PROP_DATA = "Data";

  public static final String ALS_TRIP_DATE = "TripDate";
  public static final String ALS_ORDER_DATE = "OrderDate";

  public static final String ALS_TRIP_DATE_FROM = "TripDateFrom";
  public static final String ALS_TRIP_DATE_TO = "TripDateTo";

  public static final String ALS_TRIP_DRIVER_FROM = "TripDriverDateFrom";
  public static final String ALS_TRIP_DRIVER_TO = "TripDriverDateTo";

  public static final String ALS_VEHICLE_NUMBER = "VehicleNumber";
  public static final String ALS_TRAILER_NUMBER = "TrailerNumber";

  public static final String ALS_CARGO_TRIP_VERSION = "CargoTripVer";

  public static final String ALS_ABSENCE_NAME = "AbsenceName";
  public static final String ALS_ABSENCE_LABEL = "AbsenceLabel";
  
  public static final String DATA_TYPE_CARGO = "Cargo";

  public static final String defaultLoadingColumnAlias(String colName) {
    return "DefLoad" + colName;
  }

  public static final String defaultUnloadingColumnAlias(String colName) {
    return "DefUnload" + colName;
  }

  public static final String loadingColumnAlias(String colName) {
    return "Loading" + colName;
  }

  public static final String unloadingColumnAlias(String colName) {
    return "Unloading" + colName;
  }

  private TransportConstants() {
  }
}
