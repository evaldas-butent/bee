package com.butent.bee.shared;

@FunctionalInterface
public interface Consumer<T> {
  void accept(T input);
}
