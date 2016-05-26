package com.butent.bee.shared.ui;

import com.google.common.base.Function;

import com.butent.bee.shared.data.value.Value;

@FunctionalInterface
public interface HasValueFormatter {
  Function<Value, String> getValueFormatter();
}
