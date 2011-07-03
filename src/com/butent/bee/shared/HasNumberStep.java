package com.butent.bee.shared;

/**
 * Requires implementing classes to have methods for getting and setting step value.
 */

public interface HasNumberStep {

  Number getStepValue();

  void setStepValue(Number stepValue);
}
