package com.butent.bee.shared.data.value;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * The {@code NumberValue} class represents number values. The value is set
 * using {@link com.butent.bee.shared.data.value.NumberValue#NumberValue(Double)}
 * constructor.
 */
public class NumberValue extends Value {

  private static final NumberValue NULL_VALUE = new NumberValue(null);

  public static NumberValue getNullValue() {
    return NULL_VALUE;
  }

  private Double value;

  public NumberValue(Double value) {
    this.value = value;
  }

  public int compareTo(Value o) {
    int diff = precompareTo(o);
    if (diff == BeeConst.COMPARE_UNKNOWN) {
      diff = getDouble().compareTo(o.getDouble());
    }
    return diff;
  }
  
  @Override
  public Double getDouble() {
    return value;
  }

  @Override
  public Number getObjectValue() {
    if (isNull()) {
      return null;
    }
    return value;
  }

  @Override
  public ValueType getType() {
    return ValueType.NUMBER;
  }

  @Override
  public int hashCode() {
    if (isNull()) {
      return 0;
    }
    return getDouble().hashCode();
  }

  @Override
  public boolean isNull() {
    return this == NULL_VALUE || getDouble() == null;
  }

  @Override
  public String toString() {
    if (isNull()) {
      return BeeConst.NULL;
    }
    return BeeUtils.toString(value);
  }
}
