package com.butent.bee.client.modules.calendar;

import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import java.util.List;

class AppointmentTimeRenderer extends AbstractCellRenderer {
  
  private static final DateTimeFormat DATE_FORMAT =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
  private static final DateTimeFormat DATE_TIME_FORMAT =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);

  private final int dateIndex;
  private final int dateTimeIndex;
  private final int effectiveIndex;

  AppointmentTimeRenderer(List<? extends IsColumn> columns, boolean start) {
    super(null);
    
    String name = start ? CalendarConstants.COL_START_DATE : CalendarConstants.COL_END_DATE;
    this.dateIndex = DataUtils.getColumnIndex(name, columns);
  
    name = start ? CalendarConstants.COL_START_DATE_TIME : CalendarConstants.COL_END_DATE_TIME;
    this.dateTimeIndex = DataUtils.getColumnIndex(name, columns);

    name = start ? CalendarConstants.COL_EFFECTIVE_START : CalendarConstants.COL_EFFECTIVE_END;
    this.effectiveIndex = DataUtils.getColumnIndex(name, columns);
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    
    if (!BeeConst.isUndef(effectiveIndex)) {
      DateTime effective = row.getDateTime(effectiveIndex);
      if (effective != null) {
        return DATE_TIME_FORMAT.format(effective);
      }
    }

    if (!BeeConst.isUndef(dateTimeIndex)) {
      DateTime dateTime = row.getDateTime(dateTimeIndex);
      if (dateTime != null) {
        return DATE_TIME_FORMAT.format(dateTime);
      }
    }

    if (!BeeConst.isUndef(dateIndex)) {
      JustDate date = row.getDate(dateIndex);
      if (date != null) {
        return DATE_FORMAT.format(date);
      }
    }
    return null;
  }
}
