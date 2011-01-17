package com.butent.bee.shared;

public class IntValue implements HasIntValue {
  private int value;
  
  public IntValue(int value) {
    this.value = value;
  }

  public int getInt() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
