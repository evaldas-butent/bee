package com.butent.bee.egg.server.datasource.datatable.value;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

public class DateValue extends Value {

  private static final DateValue NULL_VALUE = new DateValue();

  public static DateValue getNullValue() {
    return NULL_VALUE;
  }

  private int year;
  private int month;
  private int dayOfMonth;
  private Integer hashCode = null;

  public DateValue(GregorianCalendar calendar) {
    if (!calendar.getTimeZone().equals(TimeZone.getTimeZone("GMT"))) {
      throw new IllegalArgumentException(
           "Can't create DateValue from GregorianCalendar that is not GMT.");
    }
    this.year = calendar.get(GregorianCalendar.YEAR);
    this.month = calendar.get(GregorianCalendar.MONTH);
    this.dayOfMonth = calendar.get(GregorianCalendar.DAY_OF_MONTH);
  }

  public DateValue(int year, int month, int dayOfMonth) {
    GregorianCalendar calendar = new GregorianCalendar(year, month, dayOfMonth);

    if ((calendar.get(GregorianCalendar.YEAR) != year)
        || (calendar.get(GregorianCalendar.MONTH) != month)
        || (calendar.get(GregorianCalendar.DAY_OF_MONTH) != dayOfMonth)) {
      throw new IllegalArgumentException("Invalid java date (yyyy-MM-dd): "
          + year + '-' + month + '-' + dayOfMonth);
    }
    this.year = year;
    this.month = month;
    this.dayOfMonth = dayOfMonth;
  }

  private DateValue() {
    hashCode = 0;
  }

  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0;
    }
    DateValue otherDate = (DateValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherDate.isNull()) {
      return 1;
    }
    if (this.year > otherDate.year) {
      return 1;
    } else if (this.year < otherDate.year) {
      return -1;
    }
    if (this.month > otherDate.month) {
      return 1;
    } else if (this.month < otherDate.month) {
      return -1;
    }
    if (this.dayOfMonth > otherDate.dayOfMonth) {
      return 1;
    } else if (this.dayOfMonth < otherDate.dayOfMonth) {
      return -1;
    }
    return 0;
  }

  public int getDayOfMonth() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return dayOfMonth;
  }

  public int getMonth() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return month;
  }

  @Override
  public Calendar getObjectToFormat() {
    if (isNull()) {
      return null;
    }
    GregorianCalendar cal = new GregorianCalendar(year, month, dayOfMonth);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    return cal;
  }

  @Override
  public ValueType getType() {
    return ValueType.DATE;
  }

  public int getYear() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return year;
  }

  @Override
  public int hashCode() {
    if (null != hashCode) {
      return hashCode;
    }
    int hash  = 1279;
    hash = (hash * 17) + year;
    hash = (hash * 17) + month;
    hash = (hash * 17) + dayOfMonth;
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
    return String.format("%1$d-%2$02d-%3$02d", year, month + 1, dayOfMonth);
  }

  @Override
  protected String innerToQueryString() {
    return "DATE '" + year + "-" + (month + 1) + "-" + dayOfMonth + "'";
  }
}
