package com.butent.bee.shared.modules.calendar;

import com.google.common.collect.Range;
import com.google.common.primitives.Longs;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.UserData;
import com.butent.bee.shared.modules.calendar.CalendarConstants.ItemType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;

import java.util.Map;

public abstract class CalendarItem implements Comparable<CalendarItem> {
  
  @Override
  public int compareTo(CalendarItem other) {
    int result = Longs.compare(getStartMillis(), other.getStartMillis());
    if (result == BeeConst.COMPARE_EQUAL) {
      result = Longs.compare(other.getEndMillis(), getEndMillis());
    }
    return result;
  }

  public abstract String getBackground();

  public abstract String getCompactTemplate();
  
  public abstract String getCompanyName();
  
  public abstract String getDescription();
  
  public long getDuration() {
    return getEndMillis() - getStartMillis();
  }
  
  public abstract DateTime getEnd();
  
  public long getEndMillis() {
    return getEnd().getTime();
  }

  public abstract String getForeground();
  
  public abstract long getId();
  
  public abstract ItemType getItemType();
  
  public abstract String getMultiBodyTemplate();
  
  public abstract String getMultiHeaderTemplate();
  
  public Range<DateTime> getRange() {
    return Range.closedOpen(getStart(), getEnd());
  }
  
  public abstract Long getSeparatedAttendee();
  
  public abstract String getSimpleBodyTemplate();
  
  public abstract String getSimpleHeaderTemplate();
  
  public abstract DateTime getStart();
  
  public long getStartMillis() {
    return getStart().getTime();
  }
  
  public abstract String getStringTemplate();

  public abstract Map<String, String> getSubstitutes(long calendarId, Map<Long, UserData> users);
  
  public abstract Long getStyle();
  
  public abstract String getSummary();
  
  public abstract String getTitleTemplate();
  
  public boolean isMultiDay() {
    return TimeUtils.isMore(getEnd(), TimeUtils.startOfDay(getStart(), 1));
  }
}
