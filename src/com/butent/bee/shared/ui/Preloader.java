package com.butent.bee.shared.ui;

import com.butent.bee.shared.Consumer;

@FunctionalInterface
public interface Preloader extends Consumer<Runnable> {

  default boolean disposable() {
    return true;
  }
}
