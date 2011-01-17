package com.butent.bee.shared.data;

import com.butent.bee.shared.data.value.Value;

import java.util.List;

public interface IsRow {
  void addCell(boolean value);
  void addCell(double value);
  void addCell(String value);
  void addCell(IsCell cell);
  void addCell(Value value);
  
  IsRow clone();

  IsCell getCell(int index);
  List<IsCell> getCells();

  CustomProperties getProperties();
  Object getProperty(String key);

  void setCells(List<IsCell> cells);

  void setProperties(CustomProperties properties);
  void setProperty(String propertyKey, Object propertyValue);
}