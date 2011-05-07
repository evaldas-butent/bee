package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Enables using JavaScript objects to store ranges with minimum and maximum values.
 */

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