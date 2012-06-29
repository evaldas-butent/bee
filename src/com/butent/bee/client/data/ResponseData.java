package com.butent.bee.client.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.shared.data.BeeColumn;

import java.util.List;

/**
 * Holds response data in javascript arrays.
 */

public class ResponseData extends JsData<BeeColumn> {

  public ResponseData(JsArrayString data, List<BeeColumn> columns) {
    super(data, columns.size(), columns);
  }

  public ResponseData(JsArrayString data, int start, List<BeeColumn> columns) {
    super(data, start, columns);
  }
}
