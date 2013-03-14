package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.transport.charts.ChartBase.HasColorSource;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

class Freight implements HasDateRange, HasColorSource, HasShipmentInfo {

  private static final String cargoLabel = 
      Data.getColumnLabel(VIEW_ORDER_CARGO, COL_CARGO_DESCRIPTION);
  private static final String customerLabel = Data.getColumnLabel(VIEW_ORDERS, COL_CUSTOMER);
  private static final String notesLabel = Data.getColumnLabel(VIEW_ORDER_CARGO, COL_CARGO_NOTES);

  private final Long tripId;

  @SuppressWarnings("unused")
  private final Long cargoTripId;
  @SuppressWarnings("unused")
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

  private final String customerName;

  private Range<JustDate> range;

  Freight(SimpleRow row, JustDate minLoad, JustDate maxUnload) {
    this.tripId = row.getLong(COL_TRIP_ID);

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

  Long getCargoId() {
    return cargoId;
  }

  JustDate getMaxDate() {
    return BeeUtils.max(loadingDate, unloadingDate);
  }

  String getTitle(String loadInfo, String unloadInfo) {
    return ChartHelper.buildTitle(cargoLabel, cargoDescription, 
        Global.CONSTANTS.cargoLoading(), loadInfo,
        Global.CONSTANTS.cargoUnloading(), unloadInfo,
        customerLabel, customerName, notesLabel, notes);
  }

  Long getTripId() {
    return tripId;
  }

  private void setRange(Range<JustDate> range) {
    this.range = range;
  }
}
