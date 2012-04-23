package com.butent.bee.client.modules.calendar;

import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.calendar.CalendarConstants.TimeBlockClick;

import java.util.List;

public class CalendarSettings {

  private int pixelsPerInterval;
  private int intervalsPerHour;

  private int workingHourStart;
  private int workingHourEnd;
  private int scrollToHour;
  
  private int defaultDisplayedDays;
  
  private boolean offsetHourLabels;

  private boolean enableDragDrop;
  private boolean dragDropCreation;

  private TimeBlockClick timeBlockClickNumber;

  public CalendarSettings(IsRow row, List<? extends IsColumn> columns) {
    update(row, columns);
  }

  public int getDefaultDisplayedDays() {
    return defaultDisplayedDays;
  }

  public int getIntervalsPerHour() {
    return intervalsPerHour;
  }

  public int getPixelsPerInterval() {
    return pixelsPerInterval;
  }

  public int getScrollToHour() {
    return scrollToHour;
  }

  public TimeBlockClick getTimeBlockClickNumber() {
    return timeBlockClickNumber;
  }

  public int getWorkingHourEnd() {
    return workingHourEnd;
  }

  public int getWorkingHourStart() {
    return workingHourStart;
  }

  public boolean isDragDropCreationEnabled() {
    return dragDropCreation;
  }

  public boolean isDragDropEnabled() {
    return enableDragDrop;
  }

  public boolean offsetHourLabels() {
    return offsetHourLabels;
  }

  public void setDefaultDisplayedDays(int defaultDisplayedDays) {
    this.defaultDisplayedDays = defaultDisplayedDays;
  }

  public void setDragDropCreationEnabled(boolean dragDropCreation) {
    this.dragDropCreation = dragDropCreation;
  }

  public void setDragDropEnabled(boolean enableDragDrop) {
    this.enableDragDrop = enableDragDrop;
  }

  public void setIntervalsPerHour(int intervals) {
    intervalsPerHour = intervals;
  }

  public void setOffsetHourLabels(boolean offsetHourLabels) {
    this.offsetHourLabels = offsetHourLabels;
  }

  public void setPixelsPerInterval(int px) {
    pixelsPerInterval = px;
  }

  public void setScrollToHour(int hour) {
    scrollToHour = hour;
  }

  public void setTimeBlockClickNumber(TimeBlockClick timeBlockClickNumber) {
    this.timeBlockClickNumber = timeBlockClickNumber;
  }

  public void setWorkingHourEnd(int end) {
    workingHourEnd = end;
  }

  public void setWorkingHourStart(int start) {
    workingHourStart = start;
  }
  
  public void update(IsRow row, List<? extends IsColumn> columns) {
    setPixelsPerInterval(getInt(row, columns, CalendarConstants.COL_PIXELS_PER_INTERVAL,
        CalendarConstants.DEFAULT_PIXELS_PER_INTERVAL));
    setIntervalsPerHour(getInt(row, columns, CalendarConstants.COL_INTERVALS_PER_HOUR,
        CalendarConstants.DEFAULT_INTERVALS_PER_HOUR));

    setWorkingHourStart(getInt(row, columns, CalendarConstants.COL_WORKING_HOUR_START,
        CalendarConstants.DEFAULT_WORKING_HOUR_START));
    setWorkingHourEnd(getInt(row, columns, CalendarConstants.COL_WORKING_HOUR_END,
        CalendarConstants.DEFAULT_WORKING_HOUR_END));
    setScrollToHour(getInt(row, columns, CalendarConstants.COL_SCROLL_TO_HOUR,
        CalendarConstants.DEFAULT_SCROLL_TO_HOUR));

    setDefaultDisplayedDays(getInt(row, columns, CalendarConstants.COL_DEFAULT_DISPLAYED_DAYS,
        CalendarConstants.DEFAULT_DISPLAYED_DAYS));

    setDragDropEnabled(getBool(row, columns, CalendarConstants.COL_ENABLE_DRAG_DROP,
        CalendarConstants.DEFAULT_ENABLE_DRAG_DROP));
    setDragDropCreationEnabled(getBool(row, columns, CalendarConstants.COL_DRAG_DROP_CREATION,
        CalendarConstants.DEFAULT_DRAG_DROP_CREATION));

    setOffsetHourLabels(getBool(row, columns, CalendarConstants.COL_OFFSET_HOUR_LABELS,
        CalendarConstants.DEFAULT_OFFSET_HOUR_LABELS));

    int tbcn = getInt(row, columns, CalendarConstants.COL_TIME_BLOCK_CLICK_NUMBER,
        CalendarConstants.DEFAULT_TIME_BLOCK_CLICK_NUMBER.ordinal());
    setTimeBlockClickNumber(TimeBlockClick.values()[tbcn]);
  }
  
  private boolean getBool(IsRow row, List<? extends IsColumn> columns, String columnId,
      boolean def) {
    Boolean value = row.getBoolean(DataUtils.getColumnIndex(columnId, columns));
    return (value == null) ? def : value;
  }

  private int getInt(IsRow row, List<? extends IsColumn> columns, String columnId, int def) {
    Integer value = row.getInteger(DataUtils.getColumnIndex(columnId, columns));
    return (value == null) ? def : value;
  }
}
