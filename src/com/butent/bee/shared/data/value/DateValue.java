package com.butent.bee.shared.data.value;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.JustDate;

/**
 * The {@code DateTimeValue} class represents date values. It allows 
 * the interpretation of dates as year, month and day values.
 */
public class DateValue extends Value {
  private static final DateValue NULL_VALUE = new DateValue(null);

  public static DateValue getNullValue() {
    return NULL_VALUE;
  }

  private JustDate value;

  public DateValue(JustDate date) {
    this.value = date;
  }

  public DateValue(int year, int month, int dayOfMonth) {
    this(new JustDate(year, month, dayOfMonth));
  }

  public int compareTo(Value o) {
    int diff = precompareTo(o);
    if (diff == BeeConst.COMPARE_UNKNOWN) {
      diff = getDate().compareTo(o.getDate());
    }
    return diff;
  }

  @Override
  public JustDate getDate() {
    return value;
  }

  @Override
  public JustDate getObjectValue() {
    if (isNull()) {
      return null;
    }
    return value;
  }

  @Override
  public ValueType getType() {
    return ValueType.DATE;
  }

  @Override
  public int hashCode() {
    if (isNull()) {
      return -1;
    }
    return value.hashCode();
  }

  @Override
  public boolean isNull() {
    return this == NULL_VALUE || getDate() == null;
  }

  @Override
  public String toString() {
    if (isNull()) {
      return BeeConst.NULL;
    }
    return value.toString();
  }
}
