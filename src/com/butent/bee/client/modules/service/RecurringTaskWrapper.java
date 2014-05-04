package com.butent.bee.client.modules.service;

import com.google.common.collect.Range;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskPriority;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

public class RecurringTaskWrapper implements HasDateRange {

  private static final String idColumn = Data.getIdColumn(TaskConstants.VIEW_RECURRING_TASKS);
  
  private final Long id;
  
  private final String summary;

  private final TaskPriority priority;
  
  private final Range<JustDate> range;
  
  RecurringTaskWrapper(SimpleRow row) {
    this.id = row.getLong(idColumn);

    this.summary = row.getValue(TaskConstants.COL_SUMMARY);

    this.priority = EnumUtils.getEnumByIndex(TaskPriority.class,
        row.getInt(TaskConstants.COL_PRIORITY));
    
    JustDate start = row.getDate(TaskConstants.COL_RT_SCHEDULE_FROM);
    JustDate end = row.getDate(TaskConstants.COL_RT_SCHEDULE_UNTIL);
    
    this.range = Range.closed(start, BeeUtils.nvl(end, start));
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  Long getId() {
    return id;
  }

  TaskPriority getPriority() {
    return priority;
  }
  
  String getSummary() {
    return summary;
  }
}
