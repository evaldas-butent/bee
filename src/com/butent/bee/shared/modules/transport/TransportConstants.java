package com.butent.bee.shared.modules.transport;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

public class TransportConstants {

  public static enum OrderStatus implements HasCaption {
    CREATED, ACTIVATED, CONFIRMED, CANCELED, COMPLETED;

    public String getCaption() {
      return BeeUtils.proper(this.name(), null);
    }
  }

  public static final String TRANSPORT_MODULE = "Transport";
  public static final String TRANSPORT_METHOD = TRANSPORT_MODULE + "Method";

  public static final String SVC_GET_BEFORE = "GetBeforeData";
  public static final String SVC_GET_PROFIT = "GetProfit";

  public static final String VAR_TRIP_ID = Service.RPC_VAR_PREFIX + "trip_id";
  public static final String VAR_CARGO_ID = Service.RPC_VAR_PREFIX + "cargo_id";
  public static final String VAR_ORDER_ID = Service.RPC_VAR_PREFIX + "order_id";

  public static final String VIEW_ORDERS = "TransportationOrders";

  public static final String VIEW_CARGO = "OrderCargo";
  public static final String VIEW_CARGO_SERVICES = "CargoServices";
  public static final String VIEW_CARGO_TRIPS = "CargoTrips";

  public static final String VIEW_TRIPS = "Trips";
  public static final String VIEW_TRIP_CARGO = "TripCargo";
  public static final String VIEW_TRIP_ROUTES = "TripRoutes";
  public static final String VIEW_TRIP_COSTS = "TripCosts";
  public static final String VIEW_TRIP_FUEL_COSTS = "TripFuelCosts";
  public static final String VIEW_TRIP_FUEL_CONSUMPTIONS = "TripFuelConsumptions";

  public static final String VIEW_VEHICLES = "Vehicles";
  public static final String VIEW_FUEL_CONSUMPTIONS = "FuelConsumptions";
  public static final String VIEW_FUEL_TEMPERATURES = "FuelTemperatures";

  public static final String VIEW_SPARE_PARTS = "SpareParts";

  public static final String COL_STATUS = "Status";
  public static final String COL_OWNER = "Owner";
  public static final String COL_OWNER_NAME = "OwnerName";
  public static final String COL_PARENT_MODEL_NAME = "ParentModelName";
  public static final String COL_MODEL_NAME = "ModelName";
  public static final String COL_NUMBER = "Number";

  public static final String FORM_NEW_VEHICLE = "Vehicle";
  public static final String FORM_ORDER = "TransportationOrder";
  public static final String FORM_TRIP = "Trip";
  public static final String FORM_CARGO = "OrderCargo";

  private TransportConstants() {
  }
}
