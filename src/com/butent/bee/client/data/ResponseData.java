package com.butent.bee.client.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.shared.data.BeeColumn;

public class ResponseData extends JsData {

  public ResponseData(JsArrayString data, int columnCount) {
    this(data, columnCount, columnCount);
  }

  public ResponseData(JsArrayString data, int columnCount, int start) {
    super(data, columnCount, start);

    BeeColumn[] arr = new BeeColumn[columnCount];
    for (int i = 0; i < columnCount; i++) {
      arr[i] = BeeColumn.restore(data.get(i));
    }

    setColumns(arr);
  }

}
