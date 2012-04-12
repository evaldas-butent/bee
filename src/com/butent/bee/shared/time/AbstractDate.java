package com.butent.bee.shared.time;

import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Date;

/**
 * Implements {@code HasDateValue} interface and enables assertions and transformations for data
 * types.
 */

public abstract class AbstractDate implements HasDateValue {

  public static AbstractDate fromJava(Date date, ValueType type) {
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

  public static AbstractDate parse(String s, ValueType type) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    assertType(type);

    switch (type) {
      case DATE:
        return JustDate.parse(s);
      case DATETIME:
        return DateTime.parse(s);
      default:
        Assert.untouchable();
        return null;
    }
  }

  public static AbstractDate parse(DateTimeFormat format, String s, ValueType type) {
    if (format == null) {
      return parse(s, type);
    }
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    assertType(type);
    
    AbstractDate result;

    switch (type) {
      case DATE:
        result = Format.parseDateQuietly(format, s);
        break;
      case DATETIME:
        result = Format.parseDateTimeQuietly(format, s);
        break;
      default:
        Assert.untouchable();
        result = null;
    }
    
    if (result == null) {
      result = parse(s, type);
    }
    return result;
  }
  
  public static AbstractDate restore(String s, ValueType type) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    assertType(type);

    switch (type) {
      case DATE:
        return TimeUtils.toDateOrNull(s);
      case DATETIME:
        return TimeUtils.toDateTimeOrNull(s);
      default:
        Assert.untouchable();
        return null;
    }
  }
  
  private static void assertType(ValueType type) {
    Assert.notNull(type);
    Assert.isTrue(type.equals(ValueType.DATE) || type.equals(ValueType.DATETIME));
  }

  public abstract void deserialize(String s);
  
  public HasDateValue fromDate(JustDate justDate) {
    if (justDate == null) {
      return null;
    }
    ValueType type = getType();
    assertType(type);

    switch (type) {
      case DATE:
        return new JustDate(justDate.getDays());
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

  public int getHour() {
    return 0;
  }

  public abstract Date getJava();

  public int getMillis() {
    return 0;
  }

  public int getMinute() {
    return 0;
  }

  public int getSecond() {
    return 0;
  }

  public int getTimezoneOffset() {
    return 0;
  }

  public abstract ValueType getType();

  public abstract String serialize();
}
