package com.butent.bee.egg.shared.data.value;

public class NumberValue extends Value {
  private static final NumberValue NULL_VALUE = new NumberValue(-9999);

  public static NumberValue getNullValue() {
    return NULL_VALUE;
  }

  private double value;

  public NumberValue(double value) {
    this.value = value;
  }

  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0;
    }
    NumberValue otherNumber = (NumberValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherNumber.isNull()) {
      return 1;
    }
    return Double.compare(value, otherNumber.value);
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

  public double getValue() {
    if (this == NULL_VALUE) {
      throw new NullValueException("This null number has no value");
    }
    return value;
  }

  @Override
  public int hashCode() {
    if (isNull()) {
      return 0;
    }
    return new Double(value).hashCode();
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
    return Double.toString(value);
  }

  @Override
  protected String innerToQueryString() {
    return Double.toString(value);
  }
}
