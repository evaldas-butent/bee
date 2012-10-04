package com.butent.bee.shared.testutils;

import com.butent.bee.shared.HasIntValue;
import com.butent.bee.shared.HasLength;

/**
 * Object for testing {@link com.butent.bee.shared.HasIntValue}.
 */
public class TransObjectInt implements HasLength, HasIntValue {

  public static final double DOUBLE_DEFAULT_VALUE = 5.0;
  public int intValue = 0;
  public long longValue = 0;
  private double digit;

  @SuppressWarnings("static-access")
  public TransObjectInt() {
    setDigit(this.DOUBLE_DEFAULT_VALUE);
  }

  public double getDigit() {
    return digit;
  }

  @Override
  public int getInt() {
    return intValue;
  }

  @Override
  public int getLength() {
    return 5;
  }

  public void setDigit(double digit) {
    this.digit = digit;
  }

  @Override
  public void setValue(int value) {
    intValue = value;
  }

  @Override
  public String toString() {
    return Double.toString(this.digit);
  }
}
