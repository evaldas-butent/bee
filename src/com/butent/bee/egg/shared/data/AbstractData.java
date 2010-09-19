package com.butent.bee.egg.shared.data;

import com.butent.bee.egg.shared.utils.BeeUtils;

public abstract class AbstractData implements BeeView {
  private int columnCount = 0;
  private int rowCount = 0;
  
  private BeeColumn[] columns = null;

  public int getColumnCount() {
    return columnCount;
  }

  public String[] getColumnNames() {
    int c = getColumnCount();

    String[] arr = new String[c];
    for (int i = 0; i < c; i++) {
      if (getColumns() == null) {
        arr[i] = "Column " + BeeUtils.progress(i + 1, c);
      } else {
        arr[i] = getColumns()[i].getName();
      }
    }

    return arr;
  }

  public BeeColumn[] getColumns() {
    return columns;
  }

  public int getRowCount() {
    return rowCount;
  }

  public abstract String getValue(int row, int col);

  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
  }

  public void setColumns(BeeColumn[] columns) {
    this.columns = columns;
  }

  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }

  public abstract void setValue(int row, int col, String value);
  
}
