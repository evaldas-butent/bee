package com.butent.bee.shared.data;

import com.butent.bee.shared.data.value.Value;

public class Range {
  private Value min = null;
  private Value max = null;
  
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
