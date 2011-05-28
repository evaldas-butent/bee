package com.butent.bee.shared.data;

import com.butent.bee.shared.data.value.ValueType;

/**
 * Contains necessary methods for column classes, for example {@code getLabel} or {@code setType}.
 */

public interface IsColumn extends HasCustomProperties {

  IsColumn clone();

  String getId();

  String getLabel();

  String getPattern();

  int getPrecision();

  int getScale();

  ValueType getType();

  void setId(String id);

  void setLabel(String label);

  void setPattern(String pattern);

  void setPrecision(int precision);

  void setScale(int scale);
  
  void setType(ValueType type);
}