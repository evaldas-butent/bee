package com.butent.bee.shared.data;

import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

/**
 * Contains necessary methods for cell classes, for example {@code getFormattedValue} or
 * {@code clearValue}.
 */

public interface IsCell extends HasCustomProperties {
  void clearFormattedValue();

  void clearProperties();

  void clearValue();

  IsCell clone();

  String getFormattedValue();

  ValueType getType();

  Value getValue();

  boolean isNull();

  void setFormattedValue(String formattedValue);

  void setValue(Value value);

  void setValue(Value value, String formattedValue);
}