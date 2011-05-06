/**
 * transformuojantis objektas
 */
package com.butent.bee.shared.testutils;

import com.butent.bee.shared.HasDoubleValue;
import com.butent.bee.shared.HasLength;
import com.butent.bee.shared.Transformable;

/**
 * Object for testing {@link com.butent.bee.shared.HasDoubleValue}.
 */
public class TransObject implements Transformable, HasLength, HasDoubleValue {

  public static final double DOUBLE_DEFAULT_VALUE = 5.0;
  public double doubleValue = 0;
  public long longValue = 0;
  public int sqlval = 8088;

  private double digit;

  @SuppressWarnings("static-access")
  public TransObject() {
    setDigit(this.DOUBLE_DEFAULT_VALUE);
  }

  public double getDigit() {
    return digit;
  }

  @Override
  public double getDouble() {

    return doubleValue;
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
    return 0;
  }

  public void setDigit(double digit) {
    this.digit = digit;
  }

  @Override
  public void setValue(double value) {
    doubleValue = value;
  }

  @Override
  public void setValue(int value) {
    sqlval = value;
  }

  @Override
  public void setValue(long value) {
  }

  @Override
  public String transform() {
    return Double.toString(digit);
  }

}
