package com.butent.bee.shared;

@FunctionalInterface
public interface BiConsumer<T, U> {
  void accept(T t, U u);
}
