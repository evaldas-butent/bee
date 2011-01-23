package com.butent.bee.shared.data;

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

  IsCell getCell(int index);
  List<IsCell> getCells();

  void insertCell(int index, IsCell cell);
  void removeCell(int index);
  
  void setCell(int index, IsCell cell);
  void setCells(List<IsCell> cells);
}