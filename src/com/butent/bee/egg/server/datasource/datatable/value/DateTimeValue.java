package com.butent.bee.egg.server.datasource.datatable.value;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

public class DateTimeValue extends Value {
  private static final DateTimeValue NULL_VALUE = new DateTimeValue();

  public static DateTimeValue getNullValue() {
    return NULL_VALUE;
  }

  private GregorianCalendar calendar;

  private Integer hashCode = null;

  public DateTimeValue(GregorianCalendar calendar) {
    if (!calendar.getTimeZone().equals(TimeZone.getTimeZone("GMT"))) {
      throw new IllegalArgumentException(
          "Can't create DateTimeValue from GregorianCalendar that is not GMT.");
    }
    this.calendar = (GregorianCalendar) calendar.clone();
  }

  public DateTimeValue(int year, int month, int dayOfMonth, int hours,
      int minutes, int seconds, int milliseconds) {
    calendar = new GregorianCalendar(year, month, dayOfMonth, hours, minutes, seconds);
    calendar.set(GregorianCalendar.MILLISECOND, milliseconds);
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));

    if ((getYear() != year)
        || (getMonth() != month)
        || (getDayOfMonth() != dayOfMonth)
        || (getHourOfDay() != hours)
        || (getMinute() != minutes)
        || (getSecond() != seconds)
        || (getMillisecond() != milliseconds)) {
      throw new IllegalArgumentException("Invalid java date "
          + "(yyyy-MM-dd hh:mm:ss.S): "
          + year + '-' + month + '-' + dayOfMonth + ' ' + hours + ':'
          + minutes + ':' + seconds + '.' + milliseconds);
    }
  }

  private DateTimeValue() {
    hashCode = 0;
  }

  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0;
    }
    DateTimeValue otherDateTime = (DateTimeValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherDateTime.isNull()) {
      return 1;
    }
    return (calendar.compareTo(otherDateTime.getCalendar()));
  }

  public GregorianCalendar getCalendar() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return calendar;
  }

  public int getDayOfMonth() {
    return calendar.get(GregorianCalendar.DAY_OF_MONTH);
  }

  public int getHourOfDay() {
    return calendar.get(GregorianCalendar.HOUR_OF_DAY);
  }

  public int getMillisecond() {
    return calendar.get(GregorianCalendar.MILLISECOND);
  }

  public int getMinute() {
    return calendar.get(GregorianCalendar.MINUTE);
  }

  public int getMonth() {
    return calendar.get(GregorianCalendar.MONTH);
  }

  @Override
  public Calendar getObjectToFormat() {
    if (isNull()) {
      return null;
    }
    return calendar;
  }

  public int getSecond() {
    return calendar.get(GregorianCalendar.SECOND);
  }

  @Override
  public ValueType getType() {
    return ValueType.DATETIME;
  }

  public int getYear() {
    return calendar.get(GregorianCalendar.YEAR);
  }

  @Override
  public int hashCode() {
    if (null != hashCode) {
      return hashCode;
    }
    int hash = 1579;
    hash = (hash * 11) + getYear();
    hash = (hash * 11) + getMonth();
    hash = (hash * 11) + getDayOfMonth();
    hash = (hash * 11) + getHourOfDay();
    hash = (hash * 11) + getMinute();
    hash = (hash * 11) + getSecond();
    hash = (hash * 11) + getMillisecond();
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
   String result = String.format("%1$d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d",
       getYear(), getMonth() + 1, getDayOfMonth(), getHourOfDay(), getMinute(),
       getSecond());
    if (getMillisecond() > 0) {
      result += "." + String.format("%1$03d", getMillisecond());
    }
    return result;
  }

  @Override
  protected String innerToQueryString() {
    String s = "DATETIME '" + getYear() + "-" + (getMonth() + 1) + "-"
        + getDayOfMonth() + " " + getHourOfDay() + ":" + getMinute() + ":"
        + getSecond();
    int milli = getMillisecond();
    if (milli != 0) {
      s += "." + milli;
    }
    s += "'";
    return s;
  }
}
