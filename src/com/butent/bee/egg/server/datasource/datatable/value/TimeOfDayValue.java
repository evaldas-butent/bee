package com.butent.bee.egg.server.datasource.datatable.value;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

public class TimeOfDayValue extends Value {

  private static final TimeOfDayValue NULL_VALUE = new TimeOfDayValue();

  public static TimeOfDayValue getNullValue() {
    return NULL_VALUE;
  }

  private int hours;
  private int minutes;
  private int seconds;
  private int milliseconds;

  private Integer hashCode = null;

  public TimeOfDayValue(GregorianCalendar calendar) {
    if (!calendar.getTimeZone().equals(TimeZone.getTimeZone("GMT"))) {
      throw new IllegalArgumentException(
          "Can't create TimeOfDayValue from GregorianCalendar that is not GMT.");
    }
    this.hours = calendar.get(GregorianCalendar.HOUR_OF_DAY);
    this.minutes = calendar.get(GregorianCalendar.MINUTE);
    this.seconds = calendar.get(GregorianCalendar.SECOND);
    this.milliseconds = calendar.get(GregorianCalendar.MILLISECOND);
  }

  public TimeOfDayValue(int hours, int minutes, int seconds) {
    this(hours, minutes, seconds, 0);
  }

  public TimeOfDayValue(int hours, int minutes, int seconds, int milliseconds) {
    if ((hours >= 24) || (hours < 0)) {
      throw new IllegalArgumentException("This hours value is invalid: " + hours);
    }
    if ((minutes >= 60) || (minutes < 0)) {
      throw new IllegalArgumentException("This minutes value is invalid: " + minutes);
    }
    if ((seconds >= 60) || (seconds < 0)) {
      throw new IllegalArgumentException("This seconds value is invalid: " + seconds);
    }
    if ((milliseconds >= 1000) || (milliseconds < 0)) {
      throw new IllegalArgumentException("This milliseconds value is invalid: " + milliseconds);
    }

    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
    this.milliseconds = milliseconds;
  }

  private TimeOfDayValue() {
    hashCode = 0;
  }

  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0;
    }
    TimeOfDayValue otherTimeOfDay = (TimeOfDayValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherTimeOfDay.isNull()) {
      return 1;
    }
    if (this.hours > otherTimeOfDay.hours) {
      return 1;
    } else if (this.hours < otherTimeOfDay.hours) {
      return -1;
    }
    if (this.minutes > otherTimeOfDay.minutes) {
      return 1;
    } else if (this.minutes < otherTimeOfDay.minutes) {
      return -1;
    }
    if (this.seconds > otherTimeOfDay.seconds) {
      return 1;
    } else if (this.seconds < otherTimeOfDay.seconds) {
      return -1;
    }
    if (this.milliseconds > otherTimeOfDay.milliseconds) {
      return 1;
    } else if (this.milliseconds < otherTimeOfDay.milliseconds) {
      return -1;
    }
    return 0;
  }

  public int getHours() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return hours;
  }

  public int getMilliseconds() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return milliseconds;
  }

  public int getMinutes() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return minutes;
  }

  @Override
  public Calendar getObjectToFormat() {
    if (isNull()) {
      return null;
    }

    Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    cal.set(Calendar.YEAR, 1899);
    cal.set(Calendar.MONTH, Calendar.DECEMBER);
    cal.set(Calendar.DAY_OF_MONTH, 30);

    cal.set(Calendar.HOUR_OF_DAY, hours);
    cal.set(Calendar.MINUTE, minutes);
    cal.set(Calendar.SECOND, seconds);
    cal.set(Calendar.MILLISECOND, milliseconds);

    return cal;
  }

  public int getSeconds() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return seconds;
  }

  @Override
  public ValueType getType() {
    return ValueType.TIMEOFDAY;
  }

  @Override
  public int hashCode() {
    if (null != hashCode) {
      return hashCode;
    }
    int hash = 1193;
    hash = (hash * 13) + hours;
    hash = (hash * 13) + minutes;
    hash = (hash * 13) + seconds;
    hash = (hash * 13) + milliseconds;
    hashCode = hash;
    return hashCode;
  }

  @Override
  public boolean isNull() {
    return (this == NULL_VALUE);
  }

  @Override
  public String toString() {
    if (this == NULL_VALUE) {
      return "null";
    }
   String result = String.format("%1$02d:%2$02d:%3$02d", hours, minutes,
       seconds);
    if (milliseconds > 0) {
      result += "." + String.format("%1$3d", milliseconds);
    }
    return result;
  }

  @Override
  protected String innerToQueryString() {
    String s = "TIMEOFDAY '" + hours + ":" + minutes + ":" + seconds;
    if (milliseconds != 0) {
      s += "." + milliseconds;
    }
    s += "'";
    return s;
  }
}
