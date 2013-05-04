package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;

class CargoHandling implements HasDateRange, HasShipmentInfo {

  private static final String notesLabel =
      Data.getColumnLabel(VIEW_CARGO_HANDLING, COL_CARGO_HANDLING_NOTES);

  private final JustDate loadingDate;
  private final Long loadingCountry;
  private final String loadingPlace;
  private final String loadingTerminal;

  private final JustDate unloadingDate;
  private final Long unloadingCountry;
  private final String unloadingPlace;
  private final String unloadingTerminal;

  private final String notes;

  private final Range<JustDate> range;

  CargoHandling(SimpleRow row) {
    this.loadingDate = Places.getLoadingDate(row, loadingColumnAlias(COL_PLACE_DATE));
    this.loadingCountry = row.getLong(loadingColumnAlias(COL_COUNTRY));
    this.loadingPlace = row.getValue(loadingColumnAlias(COL_PLACE_NAME));
    this.loadingTerminal = row.getValue(loadingColumnAlias(COL_TERMINAL));

    this.unloadingDate = Places.getUnloadingDate(row, unloadingColumnAlias(COL_PLACE_DATE));
    this.unloadingCountry = row.getLong(unloadingColumnAlias(COL_COUNTRY));
    this.unloadingPlace = row.getValue(unloadingColumnAlias(COL_PLACE_NAME));
    this.unloadingTerminal = row.getValue(unloadingColumnAlias(COL_TERMINAL));

    this.notes = row.getValue(COL_CARGO_HANDLING_NOTES);

    this.range = ChartHelper.getActivity(this.loadingDate, this.unloadingDate);
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

  String getTitle(String loadInfo, String unloadInfo) {
    return ChartHelper.buildTitle(Localized.constants.intermediateLoading(), loadInfo,
        Localized.constants.intermediateUnloading(), unloadInfo,
        notesLabel, notes);
  }
}
