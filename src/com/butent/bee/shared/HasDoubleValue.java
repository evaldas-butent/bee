package com.butent.bee.shared;

/**
 * Requires any implementing classes to have get and set methods for Double number type values.
 */

public interface HasDoubleValue extends HasLongValue {
  double getDouble();

  void setValue(double value);
}
