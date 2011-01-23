package com.butent.bee.shared.data;

import com.butent.bee.shared.data.sort.SortInfo;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

import java.util.Collection;
import java.util.List;

public interface IsTable extends IsData {
  int addColumn(IsColumn column);

  int addColumn(ValueType type);
  int addColumn(ValueType type, String label);
  int addColumn(ValueType type, String label, String id);

  int addColumns(Collection<IsColumn> columnsToAdd);

  int addRow();
  int addRow(IsRow row);
  int addRow(Object... cells);

  int addRows(Collection<IsRow> rowsToAdd);
  int addRows(int rowCount);

  void addWarning(DataWarning warning);
  
  void clearCell(int rowIndex, int colIndex);
  void clearValue(int rowIndex, int colIndex);

  IsTable clone();

  boolean containsColumn(String columnId);

  IsData fromJson(String data);
  IsData fromJson(String data, double version);

  IsCell getCell(int rowIndex, int colIndex);

  IsColumn getColumn(int colIndex);
  IsColumn getColumn(String columnId);

  List<IsColumn> getColumns();

  IsRow getRow(int rowIndex);

  List<IsRow> getRows();

  List<DataWarning> getWarnings();

  void insertColumn(int colIndex, IsColumn column);

  void insertColumn(int colIndex, ValueType type);
  void insertColumn(int colIndex, ValueType type, String label);
  void insertColumn(int colIndex, ValueType type, String label, String id);

  void insertRows(int rowIndex, Collection<IsRow> rowsToAdd);
  void insertRows(int rowIndex, int rowCount);

  void removeColumn(int colIndex);
  void removeColumns(int colIndex, int colCount);

  void removeRow(int rowIndex);
  void removeRows(int rowIndex, int rowCount);

  void setCell(int rowIndex, int colIndex, boolean value);
  void setCell(int rowIndex, int colIndex, boolean value, String formattedValue);
  void setCell(int rowIndex, int colIndex, boolean value, String formattedValue,
      CustomProperties properties);

  void setCell(int rowIndex, int colIndex, double value);
  void setCell(int rowIndex, int colIndex, double value, String formattedValue);
  void setCell(int rowIndex, int colIndex, double value, String formattedValue,
      CustomProperties properties);

  void setCell(int rowIndex, int colIndex, IsCell cell);

  void setCell(int rowIndex, int colIndex, String value);
  void setCell(int rowIndex, int colIndex, String value, String formattedValue);
  void setCell(int rowIndex, int colIndex, String value, String formattedValue,
      CustomProperties properties);

  void setCell(int rowIndex, int colIndex, Value value);
  void setCell(int rowIndex, int colIndex, Value value, String formattedValue);
  void setCell(int rowIndex, int colIndex, Value value, String formattedValue,
      CustomProperties properties);

  void setColumnLabel(int colIndex, String label);
  
  void setColumnProperties(int colIndex, CustomProperties properties);
  void setColumnProperty(int colIndex, String name, Object value);

  void setFormattedValue(int rowIndex, int colIndex, String formattedValue);  

  void setProperties(int rowIndex, int colIndex, CustomProperties properties);
  void setProperty(int rowIndex, int colIndex, String name, Object value);

  void setRowProperties(int rowIndex, CustomProperties properties);
  void setRowProperty(int rowIndex, String name, Object value);  

  void setRows(Collection<IsRow> rows);

  void setTableProperties(CustomProperties properterties);
  void setTableProperty(String propertyKey, Object propertyValue);

  void setValue(int rowIndex, int colIndex, boolean value);
  void setValue(int rowIndex, int colIndex, double value);
  void setValue(int rowIndex, int colIndex, String value);
 
  void setValue(int rowIndex, int colIndex, Value value);

  void sort(int... colIndexes);
  void sort(SortInfo... sortInfo);
}