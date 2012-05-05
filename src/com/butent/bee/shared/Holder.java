package com.butent.bee.shared;

import com.butent.bee.shared.utils.BeeUtils;

public class Holder<T> {
  
  public static <T> Holder<T> absent() {
    return new Holder<T>(null);
  }

  public static <T> Holder<T> of(T value) {
    return new Holder<T>(value);
  }
  
  private T value;

  public Holder(T value) {
    this.value = value;
  }
  
  public boolean contains(T object) {
    return BeeUtils.equals(get(), object);
  }

  public T get() {
    return value;
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(value);
  }
  
  public boolean isNotEmpty() {
    return !BeeUtils.isEmpty(value);
  }

  public boolean isNotNull() {
    return value != null;
  }

  public boolean isNull() {
    return value == null;
  }

  public void set(T value) {
    this.value = value;
  }
}
