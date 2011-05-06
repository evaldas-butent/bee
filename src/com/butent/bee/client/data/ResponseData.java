package com.butent.bee.client.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.shared.data.BeeColumn;

/**
 * Holds response data in javascript arrays.
 */

public class ResponseData extends JsData<BeeColumn> {

  public ResponseData(JsArrayString data, BeeColumn... columns) {
    super(data, columns.length, columns);
  }

  public ResponseData(JsArrayString data, int start, BeeColumn... columns) {
    super(data, start, columns);
  }
}
