package com.butent.bee.shared;

public interface Procedure<T> {
  void call(T parameter);
}
