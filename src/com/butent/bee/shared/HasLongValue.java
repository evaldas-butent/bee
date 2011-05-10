package com.butent.bee.shared;

/**
 * Requires any implementing classes to have get and set methods for long integer values.
 */

public interface HasLongValue extends HasIntValue {
  long getLong();

  void setValue(long value);
}
