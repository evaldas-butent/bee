package com.butent.bee.shared;

/**
 * Store the {@code long} type value.
 */
public class LongValue implements HasLongValue {
  private long value;

  /**
   * Creates the {@code LongValue} object with {@code long} type value.
   * 
   * @param value
   */
  public LongValue(long value) {
    this.value = value;
  }

  /**
   * Returns {@code int} (4 bytes of size) type of stored {@code long} value.
   * 
   * @return {@code int} type of {@code long} value
   */
  public int getInt() {
    return (int) value;
  }

  /**
   * Returns the {@code long} type value stored in this object.
   */
  public long getLong() {
    return value;
  }

  /**
   * Sets a new long value of this object.
   * 
   * @param value {@code int} type value which will be stored as {@code long}
   */
  public void setValue(int value) {
    this.value = value;
  }

  /**
   * Sets a new long value of this object.
   * 
   * @param value {@code long} type value which will be stored
   */
  public void setValue(long value) {
    this.value = value;
  }
}
