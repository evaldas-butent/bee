package com.butent.bee.client.visualization.formatters;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

import com.butent.bee.client.visualization.DataTable;

/**
 * Enables using pattern formatting for columns in visualizations.
 */

public class PatternFormat extends JavaScriptObject {
  public static native PatternFormat create(String pattern) /*-{
    return new $wnd.google.visualization.PatternFormat(pattern);
  }-*/;

  protected PatternFormat() {
  }

  public final native void format(DataTable data, JsArrayInteger srcColumnIndices,
      int dstColumnIndex) /*-{
    this.format(data, srcColumnIndices, dstColumnIndex);
  }-*/;
}
