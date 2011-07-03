package com.butent.bee.shared;

/**
 * Requires implementing classes to have methods for getting and setting minimum and maximum values.
 */

public interface HasNumberBounds {

  Number getMaxValue();

  Number getMinValue();

  void setMaxValue(Number maxValue);

  void setMinValue(Number minValue);
}
