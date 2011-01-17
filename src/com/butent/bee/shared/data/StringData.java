package com.butent.bee.shared.data;

public class StringData extends AbstractData {
  String[][] data;

  public StringData(String[][] data) {
    this.data = data;

    setRowCount(data.length);
    setColumnCount(data[0].length);
  }

  public String[][] getData() {
    return data;
  }

  @Override
  public String getValue(int row, int col) {
    return data[row][col];
  }

  public void setData(String[][] data) {
    this.data = data;
  }

  @Override
  public void setValue(int row, int col, String value) {
    data[row][col] = value;
  }
 
}
