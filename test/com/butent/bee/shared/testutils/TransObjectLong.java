package com.butent.bee.shared.testutils;

import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasLength;
import com.butent.bee.shared.HasLongValue;

/**
 * Object for testing {@link com.butent.bee.shared.HasLongValue}.
 */
public class TransObjectLong implements BeeSerializable, HasLength, HasLongValue {

  public static final double DOUBLE_DEFAULT_VALUE = 5.0;
  public double doubleValue = 0;
  public long longValue = 0;
  private double digit;

  public TransObjectLong() {
    setDigit(DOUBLE_DEFAULT_VALUE);
  }

  @Override
  public void deserialize(String s) {
  }

  public double getDigit() {
    return digit;
  }

  @Override
  public int getInt() {
    return 0;
  }

  @Override
  public int getLength() {
    return 5;
  }

  @Override
  public long getLong() {
    return longValue;
  }

  @Override
  public String serialize() {
    return "Hello world";
  }

  public void setDigit(double digit) {
    this.digit = digit;
  }

  @Override
  public void setValue(int value) {
  }

  @Override
  public void setValue(long value) {
    longValue = value;
  }

  @Override
  public String toString() {
    return Double.toString(this.digit);
  }
}
