package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

class Freight implements HasDateRange, HasColorSource, HasShipmentInfo {

  private final Long tripId;

  private final Long truckId;
  private final Long trailerId;

  private final Long cargoTripId;
  private final Long cargoTripVersion;

  private final Long cargoId;
  private final String cargoDescription;

  private final String notes;

  private final JustDate loadingDate;
  private final Long loadingCountry;
  private final String loadingPlace;
  private final String loadingTerminal;

  private final JustDate unloadingDate;
  private final Long unloadingCountry;
  private final String unloadingPlace;
  private final String unloadingTerminal;

  private final Long orderId;

  private final OrderStatus orderStatus;
  private final DateTime orderDate;
  private final String orderNo;

  private final Long customerId;
  private final String customerName;

  private Range<JustDate> range;

  private String tripTitle = null;

  Freight(SimpleRow row, JustDate minLoad, JustDate maxUnload) {
    this.tripId = row.getLong(COL_TRIP_ID);

    this.truckId = row.getLong(COL_VEHICLE);
    this.trailerId = row.getLong(COL_TRAILER);

    this.cargoTripId = row.getLong(COL_CARGO_TRIP_ID);
    this.cargoTripVersion = row.getLong(ALS_CARGO_TRIP_VERSION);

    this.cargoId = row.getLong(COL_CARGO);
    this.cargoDescription = row.getValue(COL_CARGO_DESCRIPTION);

    this.notes = row.getValue(COL_CARGO_NOTES);

    this.loadingDate = BeeUtils.nvl(row.getDate(loadingColumnAlias(COL_PLACE_DATE)),
        row.getDate(defaultLoadingColumnAlias(COL_PLACE_DATE)), minLoad);
    this.loadingCountry = BeeUtils.nvl(row.getLong(loadingColumnAlias(COL_COUNTRY)),
        row.getLong(defaultLoadingColumnAlias(COL_COUNTRY)));
    this.loadingPlace = BeeUtils.nvl(row.getValue(loadingColumnAlias(COL_PLACE_NAME)),
        row.getValue(defaultLoadingColumnAlias(COL_PLACE_NAME)));
    this.loadingTerminal = BeeUtils.nvl(row.getValue(loadingColumnAlias(COL_TERMINAL)),
        row.getValue(defaultLoadingColumnAlias(COL_TERMINAL)));

    this.unloadingDate = BeeUtils.nvl(row.getDate(unloadingColumnAlias(COL_PLACE_DATE)),
        row.getDate(defaultUnloadingColumnAlias(COL_PLACE_DATE)), maxUnload);
    this.unloadingCountry = BeeUtils.nvl(row.getLong(unloadingColumnAlias(COL_COUNTRY)),
        row.getLong(defaultUnloadingColumnAlias(COL_COUNTRY)));
    this.unloadingPlace = BeeUtils.nvl(row.getValue(unloadingColumnAlias(COL_PLACE_NAME)),
        row.getValue(defaultUnloadingColumnAlias(COL_PLACE_NAME)));
    this.unloadingTerminal = BeeUtils.nvl(row.getValue(unloadingColumnAlias(COL_TERMINAL)),
        row.getValue(defaultUnloadingColumnAlias(COL_TERMINAL)));

    this.orderId = row.getLong(COL_ORDER);

    this.orderStatus = NameUtils.getEnumByIndex(OrderStatus.class, row.getInt(COL_STATUS));
    this.orderDate = row.getDateTime(ALS_ORDER_DATE);
    this.orderNo = row.getValue(COL_ORDER_NO);

    this.customerId = row.getLong(COL_CUSTOMER);
    this.customerName = row.getValue(COL_CUSTOMER_NAME);

    this.range = ChartHelper.getActivity(this.loadingDate, this.unloadingDate);
  }

  @Override
  public Long getColorSource() {
    return tripId;
  }

  @Override
  public Long getLoadingCountry() {
    return loadingCountry;
  }

  @Override
  public JustDate getLoadingDate() {
    return loadingDate;
  }

  @Override
  public String getLoadingPlace() {
    return loadingPlace;
  }

  @Override
  public String getLoadingTerminal() {
    return loadingTerminal;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  @Override
  public Long getUnloadingCountry() {
    return unloadingCountry;
  }

  @Override
  public JustDate getUnloadingDate() {
    return unloadingDate;
  }

  @Override
  public String getUnloadingPlace() {
    return unloadingPlace;
  }

  @Override
  public String getUnloadingTerminal() {
    return unloadingTerminal;
  }

  void adjustRange(Range<JustDate> tripRange) {
    if (tripRange == null) {
      return;
    }
    if (loadingDate != null && unloadingDate != null) {
      return;
    }

    JustDate lower = BeeUtils.nvl(loadingDate, BeeUtils.getLowerEndpoint(tripRange));
    JustDate upper = BeeUtils.nvl(unloadingDate, BeeUtils.getUpperEndpoint(tripRange));

    setRange(ChartHelper.getActivity(lower, upper));
  }

  String getCargoDescription() {
    return cargoDescription;
  }

  Long getCargoId() {
    return cargoId;
  }

  Long getCargoTripId() {
    return cargoTripId;
  }

  Long getCargoTripVersion() {
    return cargoTripVersion;
  }

  Long getCustomerId() {
    return customerId;
  }

  String getCustomerName() {
    return customerName;
  }

  JustDate getMaxDate() {
    return BeeUtils.max(loadingDate, unloadingDate);
  }

  DateTime getOrderDate() {
    return orderDate;
  }

  Long getOrderId() {
    return orderId;
  }

  String getOrderNo() {
    return orderNo;
  }
  
  OrderStatus getOrderStatus() {
    return orderStatus;
  }

  String getTitle(String loadInfo, String unloadInfo, boolean appendTripTitle) {
    String title = ChartHelper.buildTitle(OrderCargo.cargoLabel, cargoDescription,
        Global.CONSTANTS.cargoLoading(), loadInfo,
        Global.CONSTANTS.cargoUnloading(), unloadInfo,
        OrderCargo.customerLabel, customerName, OrderCargo.notesLabel, notes);

    if (appendTripTitle && !BeeUtils.isEmpty(getTripTitle())) {
      return BeeUtils.buildLines(title, BeeConst.STRING_NBSP, getTripTitle());
    } else {
      return title;
    }
  }

  Long getTrailerId() {
    return trailerId;
  }

  Long getTripId() {
    return tripId;
  }

  String getTripTitle() {
    return tripTitle;
  }

  Long getTruckId() {
    return truckId;
  }

  Long getVehicleId(VehicleType vehicleType) {
    switch (vehicleType) {
      case TRUCK:
        return getTruckId();
      case TRAILER:
        return getTrailerId();
      default:
        return null;
    }
  }

  void setTripTitle(String tripTitle) {
    this.tripTitle = tripTitle;
  }

  void updateTrip(Long newTripId, boolean fire) {
    if (!DataUtils.isId(newTripId) || Objects.equal(getTripId(), newTripId)) {
      return;
    }

    String viewName = VIEW_CARGO_TRIPS;
    List<BeeColumn> columns = Data.getColumns(viewName, Lists.newArrayList(COL_TRIP));

    RowCallback callback = fire ? new RowUpdateCallback(viewName) : null;

    Queries.update(viewName, getCargoTripId(), getCargoTripVersion(), columns,
        Queries.asList(getTripId()), Queries.asList(newTripId), callback);
  }

  private void setRange(Range<JustDate> range) {
    this.range = range;
  }
}
