package com.butent.bee.shared.data;

import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.value.Value;

import java.util.List;

public interface IsRow extends HasCustomProperties {
  void addCell(boolean value);
  void addCell(double value);
  void addCell(String value);
  void addCell(IsCell cell);
  void addCell(Value value);

  void clearCell(int index);
  
  IsRow clone();
  
  Boolean getBoolean(int index);

  IsCell getCell(int index);
  List<IsCell> getCells();
  
  JustDate getDate(int index);
  DateTime getDateTime(int index);
  
  Double getDouble(int index);
  
  long getId();
  int getNumberOfCells();
  
  String getString(int index);

  Value getValue(int index);
  
  void insertCell(int index, IsCell cell);
  void removeCell(int index);
  
  void setCell(int index, IsCell cell);
  void setCells(List<IsCell> cells);

  void setValue(int index, boolean value);
  void setValue(int index, double value);
  void setValue(int index, String value);
 
  void setValue(int index, Value value);
}