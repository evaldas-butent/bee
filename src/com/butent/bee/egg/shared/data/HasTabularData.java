package com.butent.bee.egg.shared.data;

public interface HasTabularData {
  int getColumnCount();
  String[] getColumnNames();
  BeeColumn[] getColumns();
  int getRowCount();

  String getValue(int row, int col);

  void setColumnCount(int cnt);
  void setColumns(BeeColumn[] columns);
  void setRowCount(int cnt);
  void setValue(int row, int col, String value);
}
