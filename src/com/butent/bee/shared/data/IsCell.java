package com.butent.bee.shared.data;

import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

public interface IsCell {
  void clearFormattedValue();
  void clearProperties();
  void clearValue();

  IsCell clone();

  String getFormattedValue();

  CustomProperties getProperties();
  Object getProperty(String key);

  ValueType getType();
  Value getValue();

  boolean isNull();

  void setFormattedValue(String formattedValue);

  void setProperties(CustomProperties properties);
  void setProperty(String propertyKey, Object propertyValue);

  void setValue(Value value);
  void setValue(Value value, String formattedValue);
}