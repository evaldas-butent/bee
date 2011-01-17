package com.butent.bee.client.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.AbstractData;
import com.butent.bee.shared.utils.RowComparator;

import java.util.Arrays;

public class JsData extends AbstractData {
  private JsArrayString data;
  private int start = 0;

  public JsData(JsArrayString data) {
    this.data = data;
  }

  public JsData(JsArrayString data, int columnCount) {
    this(data, columnCount, 0);
  }

  public JsData(JsArrayString data, int columnCount, int start) {
    this.data = data;
    this.start = start;

    setRowCount((data.length() - start) / columnCount);
    setColumnCount(columnCount);
  }

  public JsArrayString getData() {
    return data;
  }

  public int getStart() {
    return start;
  }

  @Override
  public String getValue(int row, int col) {
    return data.get(start + row * getColumnCount() + col);
  }

  @Override
  public void setColumnCount(int columnCount) {
    super.setColumnCount(columnCount);
    updateRowCount();
  }

  public void setData(JsArrayString data) {
    this.data = data;
  }

  public void setStart(int start) {
    this.start = start;
    updateRowCount();
  }

  @Override
  public void setValue(int row, int col, String value) {
    data.set(start + row * getColumnCount() + col, value);
  }
  
  public void sort(int col, boolean up) {
    int r = getRowCount();
    if (r <= 1) {
      return;
    }
    
    int c = getColumnCount();
    Assert.isPositive(c);
    Assert.betweenExclusive(col, 0, c);
    
    String[][] arr = new String[r][c];
    for (int i = 0; i < r; i++) {
      for (int j = 0; j < c; j++) {
        arr[i][j] = getValue(i, j);
      }
    }

    Arrays.sort(arr, new RowComparator(col, up));
    
    for (int i = 0; i < r; i++) {
      for (int j = 0; j < c; j++) {
        setValue(i, j, arr[i][j]);
      }
    }
  }

  private void updateRowCount() {
    if (getColumnCount() > 0) {
      setRowCount((getData().length() - getStart()) / getColumnCount());
    }
  }

}
