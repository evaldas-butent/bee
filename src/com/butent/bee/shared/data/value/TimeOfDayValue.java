package com.butent.bee.shared.data.value;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

/**
 * The {@code DateTimeValue} class represents time values. It allows 
 * the interpretation of time as hour, minute, second, millisecond values.
 */
public class TimeOfDayValue extends Value {

  public static final char FIELD_SEPARATOR = ':';
  public static final char MILLIS_SEPARATOR = '.';
  
  private static final TimeOfDayValue NULL_VALUE = new TimeOfDayValue();

  public static TimeOfDayValue getNullValue() {
    return NULL_VALUE;
  }

  private int hours;
  private int minutes;
  private int seconds;
  private int milliseconds;

  public TimeOfDayValue(DateTime date) {
    this.hours = date.getHour();
    this.minutes = date.getMinute();
    this.seconds = date.getSecond();
    this.milliseconds = date.getMillis();
  }

  public TimeOfDayValue(int hours, int minutes, int seconds) {
    this(hours, minutes, seconds, 0);
  }

  public TimeOfDayValue(int hours, int minutes, int seconds, int milliseconds) {
    Assert.betweenExclusive(hours, 0, 24);
    Assert.betweenExclusive(minutes, 0, 60);
    Assert.betweenExclusive(seconds, 0, 60);
    Assert.betweenExclusive(milliseconds, 0, 1000);

    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
    this.milliseconds = milliseconds;
  }

  public TimeOfDayValue(String tod) {
    if (BeeUtils.isEmpty(tod)) {
      this.hours = 0;
      this.minutes = 0;
      this.seconds = 0;
      this.milliseconds = 0;
    } else {  
      int[] arr = TimeUtils.parseFields(tod);
      this.hours = arr[0];
      this.minutes = arr[1];
      this.seconds = arr[2];
      this.milliseconds = arr[3];
    }
  }
  
  private TimeOfDayValue() {
    this.hours = -1;
    this.minutes = -1;
    this.seconds = -1;
    this.milliseconds = -1;
  }

  public int compareTo(Value o) {
    int diff = precompareTo(o);
    if (diff == BeeConst.COMPARE_UNKNOWN) {
      TimeOfDayValue other = (TimeOfDayValue) o;
      diff = ComparisonChain.start().compare(getHours(), other.getHours()).compare(getMinutes(),
          other.getMinutes()).compare(getSeconds(), other.getSeconds()).compare(getMilliseconds(),
              other.getMilliseconds()).result();
    }
    return diff;
  }

  public int getHours() {
    Assert.isTrue(!isNull());
    return hours;
  }

  public int getMilliseconds() {
    Assert.isTrue(!isNull());
    return milliseconds;
  }

  public int getMinutes() {
    Assert.isTrue(!isNull());
    return minutes;
  }

  @Override
  public String getObjectValue() {
    if (isNull()) {
      return null;
    }
    return toString();
  }

  public int getSeconds() {
    Assert.isTrue(!isNull());
    return seconds;
  }

  @Override
  public ValueType getType() {
    return ValueType.TIMEOFDAY;
  }

  @Override
  public int hashCode() {
    if (isNull()) {
      return -1;
    }
    return Objects.hashCode(getHours(), getMinutes(), getSeconds(), getMilliseconds());
    }

  @Override
  public boolean isNull() {
    return (this == NULL_VALUE);
  }

  @Override
  public String toString() {
    if (isNull()) {
      return BeeConst.NULL;
    }
    StringBuilder sb = new StringBuilder(12);
    sb.append(TimeUtils.padTwo(getHours())).append(FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getMinutes())).append(FIELD_SEPARATOR);
    sb.append(TimeUtils.padTwo(getSeconds()));
    int z = getMilliseconds();
    if (z != 0) {
      sb.append(MILLIS_SEPARATOR).append(TimeUtils.millisToString(z));
    }
    return sb.toString();
  }
}
