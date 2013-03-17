package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.transport.charts.ChartBase.HasColorSource;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.transport.TransportConstants.VehicleType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

class Trip implements HasDateRange, HasColorSource {

  private static final String tripNoLabel = Data.getColumnLabel(VIEW_TRIPS, COL_TRIP_NO);
  private static final String truckLabel = Data.getColumnLabel(VIEW_TRIPS, COL_VEHICLE);
  private static final String trailerLabel = Data.getColumnLabel(VIEW_TRIPS, COL_TRAILER);
  private static final String notesLabel = Data.getColumnLabel(VIEW_TRIPS, COL_TRIP_NOTES);

  private static final String driversLabel = Data.getLocalizedCaption(VIEW_DRIVERS);
  private static final String cargosLabel = Data.getLocalizedCaption(VIEW_CARGO_TRIPS);

  private final Long tripId;
  private final Long tripVersion;
  private final String tripNo;

  private final DateTime date;
  private final JustDate plannedEndDate;
  private final JustDate dateFrom;
  private final JustDate dateTo;

  private final Long truckId;
  private final String truckNumber;
  private final Long trailerId;
  private final String trailerNumber;

  private final String notes;

  private final Range<JustDate> range;

  private final String title;

  Trip(SimpleRow row, JustDate maxDate, String drv, int cargoCount) {
    this.tripId = row.getLong(COL_TRIP_ID);
    this.tripVersion = row.getLong(ALS_TRIP_VERSION);
    this.tripNo = row.getValue(COL_TRIP_NO);

    this.date = row.getDateTime(COL_TRIP_DATE);
    this.plannedEndDate = row.getDate(COL_TRIP_PLANNED_END_DATE);
    this.dateFrom = row.getDate(COL_TRIP_DATE_FROM);
    this.dateTo = row.getDate(COL_TRIP_DATE_TO);

    this.truckId = row.getLong(COL_VEHICLE);
    this.truckNumber = row.getValue(ALS_VEHICLE_NUMBER);
    this.trailerId = row.getLong(COL_TRAILER);
    this.trailerNumber = row.getValue(ALS_TRAILER_NUMBER);

    this.notes = row.getValue(COL_TRIP_NOTES);

    JustDate start = BeeUtils.nvl(this.dateFrom, this.date.getDate());
    JustDate end = BeeUtils.nvl(this.dateTo, this.plannedEndDate, maxDate);

    this.range = Range.closed(start, BeeUtils.max(start, end));

    this.title = ChartHelper.buildTitle(
        Global.CONSTANTS.tripDuration(), ChartHelper.getRangeLabel(this.range),
        tripNoLabel, this.tripNo,
        truckLabel, this.truckNumber,
        trailerLabel, this.trailerNumber,
        driversLabel, drv,
        cargosLabel, cargoCount,
        notesLabel, this.notes);
  }

  @Override
  public Long getColorSource() {
    return tripId;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  String getTitle() {
    return title;
  }

  Long getTrailerId() {
    return trailerId;
  }

  Long getTripId() {
    return tripId;
  }

  Long getTripVersion() {
    return tripVersion;
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
}
