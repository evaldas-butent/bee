package com.butent.bee.shared.data.value;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

public class DateValue extends Value {
  private static final DateValue NULL_VALUE = new DateValue();

  public static DateValue getNullValue() {
    return NULL_VALUE;
  }

  private int year;
  private int month;
  private int dayOfMonth;
  private Integer hashCode = null;

  public DateValue(JustDate date) {
    this.year = date.getYear();
    this.month = date.getMonth();
    this.dayOfMonth = date.getDom();
  }

  public DateValue(int year, int month, int dayOfMonth) {
    JustDate date = new JustDate(year, month, dayOfMonth);
    Assert.isTrue(date.getYear() == year && date.getMonth() == month
        && date.getDom() == dayOfMonth, "Invalid date (yyyy-MM-dd): "
        + year + '-' + month + '-' + dayOfMonth);

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
    Assert.isTrue(!isNull());
    return dayOfMonth;
  }

  public int getMonth() {
    Assert.isTrue(!isNull());
    return month;
  }

  @Override
  public JustDate getObjectValue() {
    if (isNull()) {
      return null;
    }
    return new JustDate(year, month, dayOfMonth);
  }

  @Override
  public ValueType getType() {
    return ValueType.DATE;
  }

  public int getYear() {
    Assert.isTrue(!isNull());
    return year;
  }

  @Override
  public int hashCode() {
    if (null != hashCode) {
      return hashCode;
    }
    int hash = 1279;
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
    return BeeUtils.toString(year) + "-" +
        BeeUtils.toLeadingZeroes(month, 2) + "-" + BeeUtils.toLeadingZeroes(dayOfMonth, 2);
  }
}
