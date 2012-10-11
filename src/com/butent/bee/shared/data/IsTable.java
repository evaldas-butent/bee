package com.butent.bee.shared.data;

import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Sequence;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * Determines method requirements for table structure implementing classes.
 */

public interface IsTable<RowType extends IsRow, ColType extends IsColumn> extends IsData {

  int addColumn(ColType column);

  int addColumn(ValueType type);

  int addColumn(ValueType type, String label);

  int addColumn(ValueType type, String label, String id);

  int addColumns(Collection<ColType> columnsToAdd);

  int addRow();

  int addRow(RowType row);

  int addRows(Collection<RowType> rowsToAdd);

  int addRows(int rowCount);

  void addWarning(DataWarning warning);

  void clearCell(int rowIndex, int colIndex);
  
  void clearRows();

  void clearValue(int rowIndex, int colIndex);

  boolean containsColumn(String columnId);

  boolean containsRow(long rowId);

  IsTable<RowType, ColType> copy();
  
  IsTable<RowType, ColType> create();

  ColType createColumn(ValueType type, String label, String id);

  RowType createRow(long id);

  IsCell getCell(int rowIndex, int colIndex);

  ColType getColumn(int colIndex);

  ColType getColumn(String columnId);

  List<ColType> getColumns();

  RowType getRow(int rowIndex);
  
  RowType getRowById(long rowId);
  
  int getRowIndex(long rowId);

  Sequence<RowType> getRows();

  List<DataWarning> getWarnings();

  void insertColumn(int colIndex, ColType column);

  void insertColumn(int colIndex, ValueType type);

  void insertColumn(int colIndex, ValueType type, String label);

  void insertColumn(int colIndex, ValueType type, String label, String id);

  void insertRows(int rowIndex, Collection<RowType> rowsToAdd);

  void insertRows(int rowIndex, int rowCount);

  void removeColumn(int colIndex);

  void removeColumns(int colIndex, int colCount);

  void removeRow(int rowIndex);
  
  boolean removeRowById(long rowId);
  
  void removeRows(int rowIndex, int rowCount);

  void setCell(int rowIndex, int colIndex, IsCell cell);

  void setCell(int rowIndex, int colIndex, Value value);

  void setCell(int rowIndex, int colIndex, Value value, String formattedValue);

  void setCell(int rowIndex, int colIndex, Value value, String formattedValue,
      CustomProperties properties);

  void setColumnLabel(int colIndex, String label);

  void setColumnProperties(int colIndex, CustomProperties properties);

  void setColumnProperty(int colIndex, String name, String value);

  void setColumns(List<ColType> columns);

  void setFormattedValue(int rowIndex, int colIndex, String formattedValue);

  void setProperties(int rowIndex, int colIndex, CustomProperties properties);

  void setProperty(int rowIndex, int colIndex, String name, String value);

  void setRowProperties(int rowIndex, CustomProperties properties);

  void setRowProperty(int rowIndex, String name, String value);

  void setRows(Collection<RowType> rows);

  void setTableProperties(CustomProperties properterties);

  void setTableProperty(String propertyKey, String propertyValue);

  void setValue(int rowIndex, int colIndex, Boolean value);

  void setValue(int rowIndex, int colIndex, Double value);

  void setValue(int rowIndex, int colIndex, String value);

  void setValue(int rowIndex, int colIndex, Integer value);
  
  void setValue(int rowIndex, int colIndex, Long value);
  
  void setValue(int rowIndex, int colIndex, BigDecimal value);
  
  void setValue(int rowIndex, int colIndex, JustDate value);
  
  void setValue(int rowIndex, int colIndex, DateTime value);

  void setValue(int rowIndex, int colIndex, Value value);

  void sort(int... colIndexes);

  void sort(List<Pair<Integer, Boolean>> sortInfo);

  void sortByRowId(boolean ascending);
  
  boolean updateRow(RowType row);  
}