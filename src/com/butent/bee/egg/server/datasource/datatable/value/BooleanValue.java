package com.butent.bee.egg.server.datasource.datatable.value;

public class BooleanValue extends Value {

  private static final BooleanValue NULL_VALUE = new BooleanValue(false);
  public static final BooleanValue TRUE = new BooleanValue(true);
  public static final BooleanValue FALSE = new BooleanValue(false);

  public static BooleanValue getInstance(Boolean value) {
    if (value == null) {
      return NULL_VALUE;
    }
    return value ? TRUE : FALSE;
  }

  public static BooleanValue getNullValue() {
    return NULL_VALUE;
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
  public Boolean getObjectToFormat() {
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
    if (this == NULL_VALUE) {
      throw new NullValueException("This null boolean has no value");
    }
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

  @Override
  protected String innerToQueryString() {
    return value ? "true" : "false";
  }
}
