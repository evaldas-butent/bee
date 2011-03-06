package com.butent.bee.shared.data;

import com.butent.bee.shared.data.filter.RowFilter;
import com.butent.bee.shared.data.sort.SortInfo;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

import java.util.List;

public interface IsData {
  Boolean getBoolean(int rowIndex, int colIndex);

  String getColumnId(int colIndex);
  int getColumnIndex(String columnId);

  String getColumnLabel(int colIndex);
  String getColumnPattern(int colIndex);
  
  CustomProperties getColumnProperties(int colIndex);
  Object getColumnProperty(int colIndex, String name);
  
  Range getColumnRange(int colIndex);
  ValueType getColumnType(int colIndex);

  List<Value> getDistinctValues(int colIndex);

  Double getDouble(int rowIndex, int colIndex);

  int[] getFilteredRows(RowFilter... filters);

  String getFormattedValue(int rowIndex, int colIndex);
  
  int getNumberOfColumns();
  int getNumberOfRows();

  CustomProperties getProperties(int rowIndex, int colIndex);
  Object getProperty(int rowIndex, int colIndex, String name);
  
  CustomProperties getRowProperties(int rowIndex);
  Object getRowProperty(int rowIndex, String name);

  int[] getSortedRows(int... colIndexes);
  int[] getSortedRows(SortInfo... sortInfo);

  String getString(int rowIndex, int colIndex);
  
  CustomProperties getTableProperties();
  Object getTableProperty(String key);
  
  Value getValue(int rowIndex, int colIndex);

  String toJson();
}
