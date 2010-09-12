package com.butent.bee.egg.client.data;

import com.butent.bee.egg.shared.data.BeeView;
import com.google.gwt.core.client.JsArrayString;

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

  public JsArrayString getData() {
    return data;
  }

  public void setData(JsArrayString data) {
    this.data = data;
  }

  public int getColumnCount() {
    return columnCount;
  }

  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  @Override
  public String getValue(int row, int col) {
    return data.get(start + row * columnCount + col);
  }

}
