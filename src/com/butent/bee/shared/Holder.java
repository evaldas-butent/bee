package com.butent.bee.shared;

public class Holder<T> {
  
  public static <T> Holder<T> of(T value) {
    return new Holder<T>(value);
  }
  
  private T value;

  public Holder(T value) {
    this.value = value;
  }

  public T get() {
    return value;
  }

  public void set(T value) {
    this.value = value;
  }
}
