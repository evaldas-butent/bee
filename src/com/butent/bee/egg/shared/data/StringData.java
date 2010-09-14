package com.butent.bee.egg.shared.data;

public class StringData implements BeeView {
  String[][] data;

  public StringData() {
    super();
  }

  public StringData(String[][] data) {
    this();
    this.data = data;
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

}
