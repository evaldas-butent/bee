package com.butent.bee.shared;

/**
 * Requires implementing classes to have methods to set and get precision for fields.
 */

public interface HasPrecision {

  int getPrecision();

  void setPrecision(int precision);
}
