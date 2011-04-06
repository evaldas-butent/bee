package com.butent.bee.shared.data.value;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

public class DateTimeValue extends Value {
  private static final DateTimeValue NULL_VALUE = new DateTimeValue();

  public static DateTimeValue getNullValue() {
    return NULL_VALUE;
  }

  private DateTime value;

  private Integer hashCode = null;

  public DateTimeValue(DateTime date) {
    this.value = date;
  }

  public DateTimeValue(int year, int month, int dayOfMonth, int hours, int minutes, int seconds) {
    this(year, month, dayOfMonth, hours, minutes, seconds, 0);
  }
  
  public DateTimeValue(int year, int month, int dayOfMonth, int hours,
      int minutes, int seconds, int milliseconds) {
    value = new DateTime(year, month, dayOfMonth, hours, minutes, seconds);
    
    Assert.isTrue(getYear() == year && getMonth() == month && getDayOfMonth() == dayOfMonth
        && getHourOfDay() == hours && getMinute() == minutes && getSecond() == seconds
        && getMillisecond() == milliseconds,
        "Invalid date (yyyy-MM-dd hh:mm:ss.S): " + year + '-' + month + '-' + dayOfMonth + ' '
        + hours + ':' + minutes + ':' + seconds + '.' + milliseconds);
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
    return value.compareTo(otherDateTime.getDateTime());
  }

  public DateTime getDateTime() {
    Assert.isTrue(!isNull());
    return value;
  }

  public int getDayOfMonth() {
    return value.getDom();
  }

  public int getHourOfDay() {
    return value.getHour();
  }

  public int getMillisecond() {
    return value.getMillis();
  }

  public int getMinute() {
    return value.getMinute();
  }

  public int getMonth() {
    return value.getMonth();
  }

  @Override
  public DateTime getObjectValue() {
    if (isNull()) {
      return null;
    }
    return value;
  }

  public int getSecond() {
    return value.getSecond();
  }

  @Override
  public ValueType getType() {
    return ValueType.DATETIME;
  }

  public int getYear() {
    return value.getYear();
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
    String result = BeeUtils.toString(getYear()) + "-"
        + BeeUtils.toLeadingZeroes(getMonth(), 2) + "-"
        + BeeUtils.toLeadingZeroes(getDayOfMonth(), 2) + " "
        + BeeUtils.toLeadingZeroes(getHourOfDay(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getMinute(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getSecond(), 2);
    if (getMillisecond() > 0) {
      result += "." + BeeUtils.toLeadingZeroes(getMillisecond(), 3);
    }
    return result;
  }
}
