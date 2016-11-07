package com.butent.bee.shared.ui;

import java.util.function.Consumer;

@FunctionalInterface
public interface Preloader extends Consumer<Runnable> {

  default boolean disposable() {
    return true;
  }
}
