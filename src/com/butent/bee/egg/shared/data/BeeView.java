package com.butent.bee.egg.shared.data;

public interface BeeView {
  int getColumnCount();
  String[] getColumnNames();

  BeeColumn[] getColumns();
  int getRowCount();

  String getValue(int row, int col);

  void setColumnCount(int cnt);
  void setColumns(BeeColumn[] columns);
  void setRowCount(int cnt);
}
