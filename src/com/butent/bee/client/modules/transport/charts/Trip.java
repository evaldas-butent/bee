package com.butent.bee.client.modules.transport.charts;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.Queries.IdCallback;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class Trip implements HasDateRange, HasColorSource {

  private static final String VIEW_NAME = VIEW_TRIPS;

  private static final String tripNoLabel = Data.getColumnLabel(VIEW_NAME, COL_TRIP_NO);
  private static final String truckLabel = Data.getColumnLabel(VIEW_NAME, COL_VEHICLE);
  private static final String trailerLabel = Data.getColumnLabel(VIEW_NAME, COL_TRAILER);
  private static final String notesLabel = Data.getColumnLabel(VIEW_NAME, COL_TRIP_NOTES);

  private static final String driversLabel = Data.getLocalizedCaption(VIEW_DRIVERS);
  private static final String cargosLabel = Data.getLocalizedCaption(VIEW_CARGO_TRIPS);

  static void createForCargo(final Vehicle truck, final HasShipmentInfo cargo, String cargoTitle,
      final boolean fire, final IdCallback callback) {

    if (truck == null || BeeUtils.isEmpty(cargoTitle) || callback == null) {
      return;
    }

    List<String> messages = Lists.newArrayList(cargoTitle, truck.getMessage(truckLabel),
        Global.CONSTANTS.createTripForCargoQuestion());

    Global.confirm(Global.CONSTANTS.createTripForCargoCaption(), Icon.QUESTION, messages,
        new ConfirmationCallback() {
          @Override
          public void onConfirm() {

            DataInfo dataInfo = Data.getDataInfo(VIEW_NAME);
            BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);

            newRow.setValue(dataInfo.getColumnIndex(COL_VEHICLE), truck.getId());
            if (cargo != null) {
              JustDate start = BeeUtils.nvl(cargo.getLoadingDate(), cargo.getUnloadingDate());
              if (start != null && TimeUtils.isMore(start, TimeUtils.today())) {
                newRow.setValue(dataInfo.getColumnIndex(COL_TRIP_DATE), start.getDateTime());
              }
            }

            Queries.insert(VIEW_NAME, dataInfo.getColumns(), newRow, new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                if (fire) {
                  BeeKeeper.getBus().fireEvent(new RowInsertEvent(VIEW_NAME, result));
                }

                callback.onSuccess(result.getId());
              }
            });
          }
        });
  }
  
  static String getVehicleLabel(VehicleType vehicleType) {
    switch (vehicleType) {
      case TRUCK:
        return truckLabel;
      case TRAILER:
        return trailerLabel;
      default:
        return null;
    }
  }

  static void maybeAssignCargo(String cargoMessage, String tripMessage, 
      final ConfirmationCallback callback) {
    if (BeeUtils.isEmpty(cargoMessage) || BeeUtils.isEmpty(tripMessage) || callback == null) {
      return;
    }

    Global.confirm(Global.CONSTANTS.assignCargoToTripCaption(), Icon.QUESTION,
        Lists.newArrayList(cargoMessage, tripMessage, Global.CONSTANTS.assignCargoToTripQuestion()),
        callback);
  }

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
  
  private final Collection<Driver> drivers;
  private final String title;

  Trip(SimpleRow row, JustDate maxDate, Collection<Driver> drivers, int cargoCount) {
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
    
    this.drivers = drivers;

    this.title = ChartHelper.buildTitle(
        Global.CONSTANTS.tripDuration(), ChartHelper.getRangeLabel(this.range),
        tripNoLabel, this.tripNo,
        truckLabel, this.truckNumber,
        trailerLabel, this.trailerNumber,
        driversLabel, Driver.getNames(BeeConst.DEFAULT_LIST_SEPARATOR, drivers),
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
  
  boolean hasDriver(Long driverId) {
    if (drivers != null) {
      for (Driver driver : drivers) {
        if (Objects.equal(driverId, driver.getId())) {
          return true;
        }
      }
    }
    return false; 
  }
  
  void maybeAddDriver(final Driver driver) {
    if (driver == null) {
      return;
    }
    
    final String viewName = VIEW_TRIP_DRIVERS;
    
    String driverTitle = ChartHelper.join(Data.getColumnLabel(viewName, COL_DRIVER),
        driver.getName());
    
    Global.confirm(Global.CONSTANTS.assignDriverToTripCaption(), Icon.QUESTION,
        Lists.newArrayList(driverTitle, getTitle(), Global.CONSTANTS.assignDriverToTripQuestion()),
        new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            List<BeeColumn> columns = 
                Data.getColumns(viewName, Lists.newArrayList(COL_TRIP, COL_DRIVER));
            List<String> values = Queries.asList(getTripId(), driver.getId());

            Queries.insert(viewName, columns, values, new RowInsertCallback(viewName));
          }
        });
  }

  void maybeUpdateVehicle(VehicleType vehicleType, Vehicle vehicle) {
    if (vehicleType == null || vehicle == null) {
      return;
    }

    final Long oldVehicleId = getVehicleId(vehicleType);
    final Long newVehicleId = vehicle.getId();

    if (Objects.equal(oldVehicleId, newVehicleId)) {
      return;
    }

    String caption;
    List<String> messages = Lists.newArrayList(vehicle.getMessage(getVehicleLabel(vehicleType)),
        getTitle());

    final List<BeeColumn> columns = Lists.newArrayList();

    switch (vehicleType) {
      case TRUCK:
        caption = Global.CONSTANTS.assignTruckToTripCaption();
        messages.add(Global.CONSTANTS.assignTruckToTripQuestion());

        columns.add(Data.getColumn(VIEW_NAME, COL_VEHICLE));
        break;

      case TRAILER:
        caption = Global.CONSTANTS.assignTrailerToTripCaption();
        messages.add(Global.CONSTANTS.assignTrailerToTripQuestion());

        columns.add(Data.getColumn(VIEW_NAME, COL_TRAILER));
        break;

      default:
        Assert.untouchable();
        caption = null;
    }

    Global.confirm(caption, Icon.QUESTION, messages, new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        Queries.update(VIEW_NAME, getTripId(), getTripVersion(), columns,
            Queries.asList(oldVehicleId), Queries.asList(newVehicleId),
            new RowUpdateCallback(VIEW_NAME));
      }
    });
  }
}
