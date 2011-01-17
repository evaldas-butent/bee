package com.butent.bee.shared.data;

import com.butent.bee.shared.data.filter.RowFilter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

import java.util.Collection;
import java.util.List;

public interface IsTable {
  int addColumn(IsColumn column);

  int addColumn(ValueType type);
  int addColumn(ValueType type, String label);
  int addColumn(ValueType type, String label, String id);

  int addColumns(Collection<IsColumn> columnsToAdd);

  int addRow();
  int addRow(IsRow row) throws TypeMismatchException;
  int addRow(Object... cells) throws TypeMismatchException;

  int addRows(Collection<IsRow> rowsToAdd) throws TypeMismatchException;
  int addRows(int rowCount);

  void addWarning(DataWarning warning);
  
  void clearCell(int rowIndex, int colIndex);
  void clearValue(int rowIndex, int colIndex);

  IsTable clone();

  boolean containsColumn(String columnId);

  void fromJson(String data, double version);

  IsCell getCell(int rowIndex, int colIndex);

  IsColumn getColumn(int colIndex);
  IsColumn getColumn(String columnId);

  String getColumnId(int colIndex);

  int getColumnIndex(String columnId);

  String getColumnLabel(int colIndex);
  String getColumnPattern(int colIndex);

  CustomProperties getColumnProperties(int colIndex);
  Object getColumnProperty(int colIndex, String name);

  Range getColumnRange(int colIndex);

  List<IsColumn> getColumns();

  ValueType getColumnType(int colIndex);

  List<Value> getDistinctValues(int colIndex);

  int[] getFilteredRows(RowFilter... filters);

  String getFormattedValue(int rowIndex, int colIndex);

  int getNumberOfColumns();
  int getNumberOfRows();

  CustomProperties getProperties(int rowIndex, int colIndex);
  Object getProperty(int rowIndex, int colIndex, String name);

  IsRow getRow(int rowIndex);

  CustomProperties getRowProperties(int rowIndex);
  Object getRowProperty(int rowIndex, String name);

  List<IsRow> getRows();

  int[] getSortedRows(int... colIndexes);
  int[] getSortedRows(SortInfo... sortColumns);

  CustomProperties getTableProperties();
  Object getTableProperty(String key);

  Value getValue(int rowIndex, int colIndex);

  List<DataWarning> getWarnings();

  void insertColumn(int colIndex, IsColumn column);

  void insertColumn(int colIndex, String type);
  void insertColumn(int colIndex, String type, String label);
  void insertColumn(int colIndex, String type, String label, String id);

  void insertRows(int rowIndex, Collection<IsRow> rowsToAdd) throws TypeMismatchException;
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

  void setRows(Collection<IsRow> rows) throws TypeMismatchException;

  void setTableProperties(CustomProperties properterties);
  void setTableProperty(String propertyKey, Object propertyValue);

  void setValue(int rowIndex, int colIndex, boolean value);
  void setValue(int rowIndex, int colIndex, double value);
  void setValue(int rowIndex, int colIndex, String value);
 
  void setValue(int rowIndex, int colIndex, Value value);

  void sort(int... colIndexes);
  void sort(SortInfo... sortColumns);

  String toJson();
}