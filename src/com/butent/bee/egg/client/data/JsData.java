package com.butent.bee.egg.client.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.egg.shared.data.AbstractData;

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

  private void updateRowCount() {
    if (getColumnCount() > 0) {
      setRowCount((getData().length() - getStart()) / getColumnCount());
    }
  }

}
