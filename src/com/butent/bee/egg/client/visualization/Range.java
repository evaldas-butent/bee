package com.butent.bee.egg.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;

public class Range extends JavaScriptObject {
  protected Range() {
  }

  public final native int getMax() /*-{
    return this.max;
  }-*/;

  public final native int getMin() /*-{
    return this.min;
  }-*/;
}