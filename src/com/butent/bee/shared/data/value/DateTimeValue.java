package com.butent.bee.shared.data.value;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;

/**
 * The {@code DateTimeValue} class represents date and time values. It allows 
 * the interpretation of dates as year, month, day, hour, minute,
 * second, millisecond values.
 */
public class DateTimeValue extends Value {
  private static final DateTimeValue NULL_VALUE = new DateTimeValue(null);

  public static DateTimeValue getNullValue() {
    return NULL_VALUE;
  }

  private DateTime value;

  public DateTimeValue(DateTime date) {
    this.value = date;
  }

  public DateTimeValue(int year, int month, int dayOfMonth, int hours, int minutes, int seconds) {
    this(year, month, dayOfMonth, hours, minutes, seconds, 0);
  }

  public DateTimeValue(int year, int month, int dayOfMonth, int hours,
      int minutes, int seconds, int milliseconds) {
    this.value = new DateTime(year, month, dayOfMonth, hours, minutes, seconds, milliseconds);
  }

  public int compareTo(Value o) {
    int diff = precompareTo(o);
    if (diff == BeeConst.COMPARE_UNKNOWN) {
      diff = getDateTime().compareTo(o.getDateTime());
    }
    return diff;
  }

  public DateTime getDateTime() {
    return value;
  }

  public int getDayOfMonth() {
    Assert.notNull(getDateTime());
    return getDateTime().getDom();
  }

  public int getHourOfDay() {
    Assert.notNull(getDateTime());
    return getDateTime().getHour();
  }

  public int getMillisecond() {
    Assert.notNull(getDateTime());
    return getDateTime().getMillis();
  }

  public int getMinute() {
    Assert.notNull(getDateTime());
    return getDateTime().getMinute();
  }

  public int getMonth() {
    Assert.notNull(getDateTime());
    return getDateTime().getMonth();
  }

  @Override
  public DateTime getObjectValue() {
    if (isNull()) {
      return null;
    }
    return value;
  }

  public int getSecond() {
    Assert.notNull(getDateTime());
    return getDateTime().getSecond();
  }

  @Override
  public ValueType getType() {
    return ValueType.DATETIME;
  }

  public int getYear() {
    Assert.notNull(getDateTime());
    return getDateTime().getYear();
  }

  @Override
  public int hashCode() {
    if (isNull()) {
      return -1;
    }
    return getDateTime().hashCode();
  }

  @Override
  public boolean isNull() {
    return this == NULL_VALUE || getDateTime() == null;
  }

  @Override
  public String toString() {
    if (isNull()) {
      return BeeConst.NULL;
    }
    return getDateTime().toString();
  }
}
