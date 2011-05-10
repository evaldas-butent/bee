package com.butent.bee.shared;

import com.butent.bee.shared.data.value.ValueType;

import java.util.Date;

/**
 * Implements {@code HasDateValue} interface and enables assertions and transformations for data
 * types.
 */

public abstract class AbstractDate implements HasDateValue {

  public static HasDateValue fromJava(Date date, ValueType type) {
    if (date == null) {
      return null;
    }
    assertType(type);

    switch (type) {
      case DATE:
        return new JustDate(date);
      case DATETIME:
        return new DateTime(date);
      default:
        Assert.untouchable();
        return null;
    }
  }

  private static void assertType(ValueType type) {
    Assert.notNull(type);
    Assert.isTrue(type.equals(ValueType.DATE) || type.equals(ValueType.DATETIME));
  }

  public HasDateValue fromDate(JustDate justDate) {
    if (justDate == null) {
      return null;
    }
    ValueType type = getType();
    assertType(type);

    switch (type) {
      case DATE:
        return new JustDate(justDate.getDay());
      case DATETIME:
        return new DateTime(justDate);
      default:
        Assert.untouchable();
        return null;
    }
  }

  public HasDateValue fromDateTime(DateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    ValueType type = getType();
    assertType(type);

    switch (type) {
      case DATE:
        return new JustDate(dateTime);
      case DATETIME:
        return new DateTime(dateTime.getTime());
      default:
        Assert.untouchable();
        return null;
    }
  }

  public HasDateValue fromJava(Date date) {
    return fromJava(date, getType());
  }

  public abstract JustDate getDate();

  public abstract DateTime getDateTime();

  public abstract Date getJava();

  public abstract ValueType getType();
}
