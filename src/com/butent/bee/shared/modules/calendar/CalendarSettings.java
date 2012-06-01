package com.butent.bee.shared.modules.calendar;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.calendar.CalendarConstants.TimeBlockClick;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class CalendarSettings implements BeeSerializable {

  private enum Serial {
    PIXELS_PER_INTERVAL, INTERVALS_PER_HOUR, WORKING_HOUR_START, WORKING_HOUR_END, SCROLL_TO_HOUR,
    DEFAULT_DISPLAYED_DAYS, OFFSET_HOUR_LABELS, ENABLE_DRAG_DROP, TIME_BLOCK_CLICK_NUMBER
  }

  public static CalendarSettings create(IsRow row, List<? extends IsColumn> columns) {
    CalendarSettings settings = new CalendarSettings();
    if (row != null && !BeeUtils.isEmpty(columns)) {
      settings.loadFrom(row, columns);
    }
    return settings;
  }

  public static CalendarSettings restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }

    CalendarSettings settings = new CalendarSettings();
    settings.deserialize(s);
    return settings;
  }
  
  private int pixelsPerInterval;
  private int intervalsPerHour;

  private int workingHourStart;
  private int workingHourEnd;
  private int scrollToHour;

  private int defaultDisplayedDays;

  private boolean offsetHourLabels;

  private boolean enableDragDrop;

  private TimeBlockClick timeBlockClickNumber;

  private CalendarSettings() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case DEFAULT_DISPLAYED_DAYS:
          setDefaultDisplayedDays(BeeUtils.toInt(value));
          break;
        case ENABLE_DRAG_DROP:
          setDragDropEnabled(Codec.unpack(value));
          break;
        case INTERVALS_PER_HOUR:
          setIntervalsPerHour(BeeUtils.toInt(value));
          break;
        case OFFSET_HOUR_LABELS:
          setOffsetHourLabels(Codec.unpack(value));
          break;
        case PIXELS_PER_INTERVAL:
          setPixelsPerInterval(BeeUtils.toInt(value));
          break;
        case SCROLL_TO_HOUR:
          setScrollToHour(BeeUtils.toInt(value));
          break;
        case TIME_BLOCK_CLICK_NUMBER:
          setTimeBlockClickNumber(Codec.unpack(TimeBlockClick.class, value));
          break;
        case WORKING_HOUR_END:
          setWorkingHourEnd(BeeUtils.toInt(value));
          break;
        case WORKING_HOUR_START:
          setWorkingHourStart(BeeUtils.toInt(value));
          break;
      }
    }  
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

  public boolean isDragDropEnabled() {
    return enableDragDrop;
  }

  public void loadFrom(IsRow row, List<? extends IsColumn> columns) {
    setPixelsPerInterval(getInt(row, columns, CalendarConstants.COL_PIXELS_PER_INTERVAL));
    setIntervalsPerHour(getInt(row, columns, CalendarConstants.COL_INTERVALS_PER_HOUR));

    setWorkingHourStart(getInt(row, columns, CalendarConstants.COL_WORKING_HOUR_START));
    setWorkingHourEnd(getInt(row, columns, CalendarConstants.COL_WORKING_HOUR_END));
    setScrollToHour(getInt(row, columns, CalendarConstants.COL_SCROLL_TO_HOUR));

    setDefaultDisplayedDays(getInt(row, columns, CalendarConstants.COL_DEFAULT_DISPLAYED_DAYS));

    setDragDropEnabled(getBool(row, columns, CalendarConstants.COL_ENABLE_DRAG_DROP));

    setOffsetHourLabels(getBool(row, columns, CalendarConstants.COL_OFFSET_HOUR_LABELS));

    int tbcn = getInt(row, columns, CalendarConstants.COL_TIME_BLOCK_CLICK_NUMBER);
    if (BeeUtils.isOrdinal(TimeBlockClick.class, tbcn)) {
      setTimeBlockClickNumber(TimeBlockClick.values()[tbcn]);
    } else {
      setTimeBlockClickNumber(null);
    }
  }

  public boolean offsetHourLabels() {
    return offsetHourLabels;
  }

  public void saveTo(IsRow row, List<? extends IsColumn> columns) {
    setInt(row, columns, CalendarConstants.COL_PIXELS_PER_INTERVAL, getPixelsPerInterval());
    setInt(row, columns, CalendarConstants.COL_INTERVALS_PER_HOUR, getIntervalsPerHour());

    setInt(row, columns, CalendarConstants.COL_WORKING_HOUR_START, getWorkingHourStart());
    setInt(row, columns, CalendarConstants.COL_WORKING_HOUR_END, getWorkingHourEnd());
    setInt(row, columns, CalendarConstants.COL_SCROLL_TO_HOUR, getScrollToHour());

    setInt(row, columns, CalendarConstants.COL_DEFAULT_DISPLAYED_DAYS, getDefaultDisplayedDays());

    setBool(row, columns, CalendarConstants.COL_ENABLE_DRAG_DROP, isDragDropEnabled());

    setBool(row, columns, CalendarConstants.COL_OFFSET_HOUR_LABELS, offsetHourLabels());

    int tbcn = (getTimeBlockClickNumber() == null)
        ? BeeConst.UNDEF : getTimeBlockClickNumber().ordinal();
    setInt(row, columns, CalendarConstants.COL_TIME_BLOCK_CLICK_NUMBER, tbcn);
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case DEFAULT_DISPLAYED_DAYS:
          arr[i++] = getDefaultDisplayedDays();
          break;
        case ENABLE_DRAG_DROP:
          arr[i++] = Codec.pack(isDragDropEnabled());
          break;
        case INTERVALS_PER_HOUR:
          arr[i++] = getIntervalsPerHour();
          break;
        case OFFSET_HOUR_LABELS:
          arr[i++] = Codec.pack(offsetHourLabels());
          break;
        case PIXELS_PER_INTERVAL:
          arr[i++] = getPixelsPerInterval();
          break;
        case SCROLL_TO_HOUR:
          arr[i++] = getScrollToHour();
          break;
        case TIME_BLOCK_CLICK_NUMBER:
          arr[i++] = Codec.pack(getTimeBlockClickNumber());
          break;
        case WORKING_HOUR_END:
          arr[i++] = getWorkingHourEnd();
          break;
        case WORKING_HOUR_START:
          arr[i++] = getWorkingHourStart();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setDefaultDisplayedDays(int defaultDisplayedDays) {
    this.defaultDisplayedDays = defaultDisplayedDays;
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

  private boolean getBool(IsRow row, List<? extends IsColumn> columns, String columnId) {
    Boolean value = row.getBoolean(DataUtils.getColumnIndex(columnId, columns));
    return (value == null) ? false : value;
  }

  private int getInt(IsRow row, List<? extends IsColumn> columns, String columnId) {
    Integer value = row.getInteger(DataUtils.getColumnIndex(columnId, columns));
    return (value == null) ? BeeConst.UNDEF : value;
  }

  private void setBool(IsRow row, List<? extends IsColumn> columns, String columnId, boolean b) {
    Boolean value = b ? Boolean.TRUE : null;
    row.setValue(DataUtils.getColumnIndex(columnId, columns), value);
  }

  private void setInt(IsRow row, List<? extends IsColumn> columns, String columnId, int i) {
    Integer value = BeeConst.isUndef(i) ? null : Integer.valueOf(i);
    row.setValue(DataUtils.getColumnIndex(columnId, columns), value);
  }
}
