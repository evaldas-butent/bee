package com.butent.bee.shared.data;

public interface IsView extends IsData {
  IsView create(IsData data);
  IsView create(IsData data, String viewAsJson);
  
  int getTableColumnIndex(int viewColumnIndex);
  int getTableRowIndex(int viewRowIndex);
  
  int getViewColumnIndex(int tableColumnIndex);
  int getViewColumns();
  
  int getViewRowIndex(int tableRowIndex);
  int[] getViewRows();  

  void hideColumns(int... columnIndexes);

  void hideRows(int min, int max);
  void hideRows(int...  rowIndexes);
  
  void setColumns(int... columnIndexes);
  void setColumns(Object... columns);
  
  void setRows(int min, int max);
  void setRows(int... rowIndexes);
  
  IsTable toDataTable();
}
