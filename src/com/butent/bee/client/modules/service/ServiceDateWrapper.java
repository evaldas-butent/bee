package com.butent.bee.client.modules.service;

import com.google.common.collect.Range;

import com.butent.bee.client.timeboard.TimeBoardHelper;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;

public class ServiceDateWrapper implements HasDateRange {
  
  private final DateTime from;
  private final DateTime until;

  private final Range<JustDate> range;
  
  private final String color;
  private final String note;
  
  ServiceDateWrapper(SimpleRow row) {
    this.from = row.getDateTime(ServiceConstants.COL_SERVICE_DATE_FROM);
    this.until = row.getDateTime(ServiceConstants.COL_SERVICE_DATE_UNTIL);

    this.color = row.getValue(ServiceConstants.COL_SERVICE_DATE_COLOR);
    this.note = row.getValue(ServiceConstants.COL_SERVICE_DATE_NOTE);
    
    this.range = TimeBoardHelper.getRange(from, until);
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  String getColor() {
    return color;
  }

  DateTime getFrom() {
    return from;
  }

  String getNote() {
    return note;
  }

  DateTime getUntil() {
    return until;
  }
}
