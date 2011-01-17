package com.butent.bee.shared;

public class LongValue implements HasLongValue {
  private long value;
  
  public LongValue(long value) {
    this.value = value;
  }

  public int getInt() {
    return (int) value;
  }

  public long getLong() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public void setValue(long value) {
    this.value = value;
  }
}
