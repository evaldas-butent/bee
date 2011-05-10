package com.butent.bee.shared;

/**
 * Requires any implementing classes to have get and set methods for string values.
 */

public interface HasStringValue {
  String getString();

  void setValue(String value);
}
