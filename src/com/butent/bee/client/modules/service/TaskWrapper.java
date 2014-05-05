package com.butent.bee.client.modules.service;

import com.google.common.collect.Range;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.timeboard.TimeBoardHelper;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskPriority;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskStatus;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.EnumUtils;

class TaskWrapper implements HasDateRange {

  private static final String idColumn = Data.getIdColumn(TaskConstants.VIEW_TASKS);
  
  private final Long id;
  
  private final String summary;

  private final TaskPriority priority;
  private final TaskStatus status;

  private final Range<JustDate> range;
  
  TaskWrapper(SimpleRow row) {
    this.id = row.getLong(idColumn);

    this.summary = row.getValue(TaskConstants.COL_SUMMARY);
    
    this.priority = EnumUtils.getEnumByIndex(TaskPriority.class,
        row.getInt(TaskConstants.COL_PRIORITY));
    this.status = EnumUtils.getEnumByIndex(TaskStatus.class,
        row.getInt(TaskConstants.COL_STATUS));

    DateTime start = row.getDateTime(TaskConstants.COL_START_TIME);
    DateTime end = row.getDateTime(TaskConstants.COL_FINISH_TIME);
    
    this.range = TimeBoardHelper.getRange(start, end);
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

  TaskStatus getStatus() {
    return status;
  }

  String getSummary() {
    return summary;
  }
}
