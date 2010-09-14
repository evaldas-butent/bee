package com.butent.bee.egg.client.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.egg.shared.data.BeeView;

public class JsData implements BeeView {
  JsArrayString data;
  int columnCount;
  int start;

  public JsData() {
    super();
  }

  public JsData(JsArrayString data, int columnCount) {
    this(data, columnCount, 0);
  }

  public JsData(JsArrayString data, int columnCount, int start) {
    this();
    this.data = data;
    this.columnCount = columnCount;
    this.start = start;
  }

  public int getColumnCount() {
    return columnCount;
  }

  public JsArrayString getData() {
    return data;
  }

  public int getStart() {
    return start;
  }

  @Override
  public String getValue(int row, int col) {
    return data.get(start + row * columnCount + col);
  }

  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
  }

  public void setData(JsArrayString data) {
    this.data = data;
  }

  public void setStart(int start) {
    this.start = start;
  }

}
