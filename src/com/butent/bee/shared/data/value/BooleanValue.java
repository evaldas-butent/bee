package com.butent.bee.shared.data.value;

import com.butent.bee.shared.utils.BeeUtils;

public class BooleanValue extends Value {

  public static final BooleanValue TRUE = new BooleanValue(true);
  public static final BooleanValue FALSE = new BooleanValue(false);
  private static final BooleanValue NULL_VALUE = new BooleanValue(false);

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

  private boolean value;

  private BooleanValue(boolean value) {
    this.value = value;
  }

  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0;
    }
    BooleanValue otherBoolean = (BooleanValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherBoolean.isNull()) {
      return 1;
    }
    return (value == otherBoolean.value ? 0 : (value ? 1 : -1));
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

  public boolean getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return (isNull() ? -1 : (value ? 1 : 0));
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
    return Boolean.toString(value);
  }
}
