package com.butent.bee.shared.data.value;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * The {@code BooleanValue} class represents a boolean value. These values 
 * are set by using
 * {@link com.butent.bee.shared.data.value.BooleanValue#getInstance(Boolean)} 
 * and are comparable. 
 */
public class BooleanValue extends Value {

  public static final BooleanValue TRUE = new BooleanValue(true);
  public static final BooleanValue FALSE = new BooleanValue(false);
  private static final BooleanValue NULL_VALUE = new BooleanValue(null);

  private static final String S_TRUE = "t";
  private static final String S_FALSE = "f";
  private static final String S_NULL = "n";

  public static BooleanValue getInstance(Boolean value) {
    if (value == null) {
      return NULL_VALUE;
    }
    return value ? TRUE : FALSE;
  }

  public static BooleanValue getNullValue() {
    return NULL_VALUE;
  }

  public static String pack(Boolean value) {
    if (value == null) {
      return S_NULL;
    }
    return value ? S_TRUE : S_FALSE;
  }

  public static Boolean unpack(String s) {
    if (BeeUtils.startsSame(s, S_TRUE)) {
      return true;
    }
    if (BeeUtils.startsSame(s, S_FALSE)) {
      return false;
    }
    return null;
  }

  private Boolean value;

  public BooleanValue(Boolean value) {
    this.value = value;
  }

  public int compareTo(Value o) {
    int diff = precompareTo(o);
    if (diff == BeeConst.COMPARE_UNKNOWN) {
      diff = getBoolean().compareTo(o.getBoolean());
    }
    return diff;
  }

  public Boolean getBoolean() {
    return value;
  }
  
  @Override
  public Boolean getObjectValue() {
    if (isNull()) {
      return null;
    }
    return value;
  }

  @Override
  public ValueType getType() {
    return ValueType.BOOLEAN;
  }

  @Override
  public int hashCode() {
    return (isNull() ? -1 : (value ? 1 : 0));
  }

  @Override
  public boolean isNull() {
    return this == NULL_VALUE || getBoolean() == null;
  }

  @Override
  public String toString() {
    if (isNull()) {
      return BeeConst.NULL;
    }
    return Boolean.toString(value);
  }
}
