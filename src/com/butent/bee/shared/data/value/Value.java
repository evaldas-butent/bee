package com.butent.bee.shared.data.value;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Comparator;

public abstract class Value implements Comparable<Value>, Transformable {

  public static Comparator<Value> getComparator() {
    return new Comparator<Value>() {

      @Override
      public int compare(Value value1, Value value2) {
        if (value1 == value2) {
          return 0;
        }
        if (value1 == null) {
          return -1;
        }
        if (value2 == null) {
          return 1;
        }
        return value1.compareTo(value2);
      }
    };
  }

  public static Value getNullValueFromValueType(ValueType type) {
    switch (type) {
      case BOOLEAN:
        return BooleanValue.getNullValue();
      case TEXT:
        return TextValue.getNullValue();
      case NUMBER:
        return NumberValue.getNullValue();
      case TIMEOFDAY:
        return TimeOfDayValue.getNullValue();
      case DATE:
        return DateValue.getNullValue();
      case DATETIME:
        return DateTimeValue.getNullValue();
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if ((null == o) || (this.getClass() != o.getClass())) {
      return false;
    }
    return (this.compareTo((Value) o) == 0);
  }

  public Boolean getBoolean() {
    if (isNull()) {
      return null;
    }
    switch (getType()) {
      case BOOLEAN :
        return (Boolean) getObjectValue();
      case NUMBER :
        return !BeeUtils.isZero(getObjectValue());
      case TEXT :
        return BeeUtils.toBoolean((String) getObjectValue());
      default:
        return null;
    }
  }
  
  public JustDate getDate() {
    if (isNull()) {
      return null;
    }
    switch (getType()) {
      case DATE :
        return (JustDate) getObjectValue();
      case DATETIME :
        return new JustDate((DateTime) getObjectValue());
      case NUMBER :
        return new JustDate(((Number) getObjectValue()).intValue());
      default:
        return null;
    }
  }

  public DateTime getDateTime() {
    if (isNull()) {
      return null;
    }
    switch (getType()) {
      case DATE :
        return new DateTime((JustDate) getObjectValue());
      case DATETIME :
        return (DateTime) getObjectValue();
      case NUMBER :
        return new DateTime(((Number) getObjectValue()).longValue());
      default:
        return null;
    }
  }
  
  public Double getDouble() {
    if (isNull()) {
      return null;
    }
    switch (getType()) {
      case NUMBER :
        return (Double) getObjectValue();
      case TEXT :
        return BeeUtils.toDouble((String) getObjectValue());
      default:
        return null;
    }
  }

  public abstract Object getObjectValue();
  
  public String getString() {
    if (isNull()) {
      return null;
    }
    if (getType().equals(ValueType.TEXT)) {
      return (String) getObjectValue();
    }
    return transform();
  }

  public abstract ValueType getType();

  @Override
  public abstract int hashCode();

  public abstract boolean isNull();

  public final String toQueryString() {
    Assert.isTrue(!isNull(), "Cannot run toQueryString() on a null value.");
    return innerToQueryString();
  }

  public String transform() {
    return toString();
  }

  protected abstract String innerToQueryString();
}
