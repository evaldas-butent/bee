package com.butent.bee.shared;

/**
 * Stores the {@code int} type value.
 * 
 */
public class IntValue implements HasIntValue {
  private int value;
  
  /**
   * Creates new object of {@code IntValue} with value of {@code int}
   * @param value {@code int} type value which will be stored
   */
  public IntValue(int value) {
    this.value = value;
  }

  /**
   * Returns stored {@code int} type value
   * 
   * @return {@code int} type value  
   */
  public int getInt() {
    return value;
  }

  /**
   * Sets a new {@code int} type value
   */
  public void setValue(int value) {
    this.value = value;
  }
}
