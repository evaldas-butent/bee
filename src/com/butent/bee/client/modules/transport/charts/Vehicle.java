package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

class Vehicle implements HasDateRange {

  private static final int numberIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_NUMBER);
  private static final int startIndex =
      Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_START_DATE);
  private static final int endIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_END_DATE);

  private static final int parentModelNameIndex =
      Data.getColumnIndex(VIEW_VEHICLES, COL_PARENT_MODEL_NAME);
  private static final int modelNameIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_MODEL_NAME);
  private static final int notesIndex = Data.getColumnIndex(VIEW_VEHICLES, COL_VEHICLE_NOTES);

  private static final String startLabel =
      Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_START_DATE);
  private static final String endLabel = Data.getColumnLabel(VIEW_VEHICLES, COL_VEHICLE_END_DATE);

  private final BeeRow row;

  private final Long id;
  private final String number;

  private final Range<JustDate> range;

  Vehicle(BeeRow row) {
    this.row = row;

    this.id = row.getId();
    this.number = row.getString(numberIndex);

    this.range = ChartHelper.getActivity(row.getDate(startIndex), row.getDate(endIndex));
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  Long getId() {
    return id;
  }

  String getInactivityTitle(Range<JustDate> inactivity) {
    if (inactivity == null || getRange() == null) {
      return BeeConst.STRING_EMPTY;

    } else if (inactivity.hasUpperBound() && getRange().hasLowerBound()
        && BeeUtils.isLess(inactivity.upperEndpoint(), getRange().lowerEndpoint())) {
      return ChartHelper.buildTitle(startLabel, getRange().lowerEndpoint());

    } else if (inactivity.hasLowerBound() && getRange().hasUpperBound()
        && BeeUtils.isMore(inactivity.lowerEndpoint(), getRange().upperEndpoint())) {
      return ChartHelper.buildTitle(endLabel, getRange().upperEndpoint());

    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  String getInfo() {
    return BeeUtils.joinWords(row.getString(parentModelNameIndex), row.getString(modelNameIndex),
        row.getString(notesIndex));
  }

  String getNumber() {
    return number;
  }

  String getTitle() {
    return BeeUtils.trim(row.getString(notesIndex));
  }
}
