package com.butent.bee.shared.ui;

import com.butent.bee.shared.data.value.Value;

import java.util.function.Function;

@FunctionalInterface
public interface HasValueFormatter {
  Function<Value, String> getValueFormatter();
}
