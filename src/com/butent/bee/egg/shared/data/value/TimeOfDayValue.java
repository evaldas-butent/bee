package com.butent.bee.egg.shared.data.value;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.utils.BeeUtils;

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

  public TimeOfDayValue(BeeDate date) {
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
  public BeeDate getObjectToFormat() {
    if (isNull()) {
      return null;
    }
    return new BeeDate(2011, 1, 1, hours, minutes, seconds, milliseconds);
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
    String result = BeeUtils.toLeadingZeroes(hours, 2) + ":"
        + BeeUtils.toLeadingZeroes(minutes, 2) + ":"
        + BeeUtils.toLeadingZeroes(seconds, 2);
    if (milliseconds > 0) {
      result += "." + BeeUtils.toLeadingZeroes(milliseconds, 3);
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
