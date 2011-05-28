package com.butent.bee.shared.data.value;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.math.BigDecimal;

/**
 * The {@code DateTimeValue} class represents time values. It allows the interpretation of time as
 * hour, minute, second, millisecond values.
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

  public TimeOfDayValue(int hours) {
    this(hours, 0, 0, 0);
  }

  public TimeOfDayValue(int hours, int minutes) {
    this(hours, minutes, 0, 0);
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
    this(new DateTime(BeeUtils.toLong(tod)));
  }

  private TimeOfDayValue() {
    this.hours = -1;
    this.minutes = -1;
    this.seconds = -1;
    this.milliseconds = -1;
  }

  @Override
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

  @Override
  public Boolean getBoolean() {
    if (isNull()) {
      return null;
    }
    Assert.unsupported("get boolean from timeofday");
    return null;
  }

  @Override
  public JustDate getDate() {
    if (isNull()) {
      return null;
    }
    Assert.unsupported("get date from timeofday");
    return null;
  }

  @Override
  public DateTime getDateTime() {
    if (isNull()) {
      return null;
    }
    Assert.unsupported("get datetime from timeofday");
    return null;
  }

  @Override
  public BigDecimal getDecimal() {
    if (isNull()) {
      return null;
    }
    return BeeUtils.toDecimalOrNull(getTime());
  }

  @Override
  public Double getDouble() {
    if (isNull()) {
      return null;
    }
    return (double) getTime();
  }

  public int getHours() {
    Assert.isTrue(!isNull());
    return hours;
  }

  @Override
  public Integer getInteger() {
    if (isNull()) {
      return null;
    }
    return getTime();
  }

  @Override
  public Long getLong() {
    if (isNull()) {
      return null;
    }
    return (long) getTime();
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
  public String getString() {
    if (isNull()) {
      return null;
    }
    return BeeUtils.toString(getTime());
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

  @Override
  public String transform() {
    return toString();
  }

  private int getTime() {
    return TimeUtils.getMillis(getHours(), getMinutes(), getSeconds(), getMilliseconds());
  }
}
