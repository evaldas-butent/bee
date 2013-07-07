package com.butent.bee.shared.data;

import com.butent.bee.shared.data.value.Value;

/**
 * Handles range management for data structures, containing min and max values of a range.
 */

public class Range {
  private Value min;
  private Value max;

  public Range(Value min, Value max) {
    this.min = min;
    this.max = max;
  }

  public Value getMax() {
    return max;
  }

  public Value getMin() {
    return min;
  }

  public void setMax(Value max) {
    this.max = max;
  }

  public void setMin(Value min) {
    this.min = min;
  }
}
