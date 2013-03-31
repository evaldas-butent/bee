package com.butent.bee.shared;

public interface Consumer<T> {
  void accept(T input);
}
