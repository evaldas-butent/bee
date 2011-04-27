package com.butent.bee.shared.data.value;

import com.butent.bee.shared.BeeConst;

public class TextValue extends Value {

  private static final TextValue NULL_VALUE = new TextValue(null);

  public static TextValue getNullValue() {
    return NULL_VALUE;
  }

  private String value;

  public TextValue(String value) {
    this.value = value;
  }

  public int compareTo(Value o) {
    int diff = precompareTo(o);
    if (diff == BeeConst.COMPARE_UNKNOWN) {
      diff = getString().compareTo(o.getString());
    }
    return diff;
  }

  @Override
  public String getObjectValue() {
    return value;
  }

  public String getString() {
    return value;
  }
  
  @Override
  public ValueType getType() {
    return ValueType.TEXT;
  }

  @Override
  public int hashCode() {
    if (isNull()) {
      return -1;
    }
    return getString().hashCode();
  }

  @Override
  public boolean isNull() {
    return this == NULL_VALUE || getString() == null;
  }

  @Override
  public String toString() {
    if (isNull()) {
      return BeeConst.NULL;
    }
    return getString();
  }
}
