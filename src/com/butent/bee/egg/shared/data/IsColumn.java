package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.data.value.ValueType;

public interface IsColumn {
  IsColumn clone();
  
  String getId();
  String getLabel();
  String getPattern();

  CustomProperties getProperties();
  Object getProperty(String key);

  ValueType getType();

  void setId(String id);
  void setLabel(String label);
  void setPattern(String pattern);

  void setProperties(CustomProperties properties);
  void setProperty(String propertyKey, Object propertyValue);

  void setType(ValueType type);
}