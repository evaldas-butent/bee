package com.butent.bee.client.modules.transport;

import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

class VehicleService implements HasDateRange {

  private final Long vehicleId;
  private final String vehicleNumber;

  private final String name;
  private final String notes;

  private final Range<JustDate> range;

  VehicleService(SimpleRow row) {
    super();

    this.vehicleId = row.getLong(COL_VEHICLE);
    this.vehicleNumber = row.getValue(COL_NUMBER);

    this.name = row.getValue(COL_SERVICE_NAME);
    this.notes = row.getValue(COL_SERVICE_NOTES);

    JustDate start = row.getDate(COL_SERVICE_DATE);
    JustDate end = row.getDate(COL_SERVICE_DATE_TO);

    this.range = Range.closed(start, BeeUtils.max(start, end));
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  String getName() {
    return name;
  }

  String getNotes() {
    return notes;
  }

  Long getVehicleId() {
    return vehicleId;
  }

  String getVehicleNumber() {
    return vehicleNumber;
  }
}
