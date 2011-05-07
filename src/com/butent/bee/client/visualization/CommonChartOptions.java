package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Sets widely used chart options like axis color or Y axis title.
 */

public class CommonChartOptions extends CommonOptions {
  public static CommonChartOptions create() {
    return JavaScriptObject.createObject().cast();
  }

  protected CommonChartOptions() {
  }

  public final native void setAxisBackgroundColor(Color color) /*-{
    this.axisBackgroundColor = color;
  }-*/;

  public final native void setAxisBackgroundColor(String color) /*-{
    this.axisBackgroundColor = color;
  }-*/;

  public final native void setAxisColor(Color color) /*-{
    this.axisColor = color;
  }-*/;

  public final native void setAxisColor(String color) /*-{
    this.axisColor = color;
  }-*/;

  public final native void setAxisFontSize(double size) /*-{
    this.axisFontSize = size;
  }-*/;

  public final native void setLogScale(boolean logScale) /*-{
    this.logScale = logScale;
  }-*/;

  public final native void setMax(double max) /*-{
    this.max = max;
  }-*/;

  public final native void setMin(double min) /*-{
    this.min = min;
  }-*/;

  public final native void setReverseAxis(boolean reverseAxis) /*-{
    this.reverseAxis = reverseAxis;
  }-*/;

  public final native void setShowCategories(boolean showCategories) /*-{
    this.showCategories = showCategories;
  }-*/;

  public final native void setTitleX(String title) /*-{
    this.titleX = title;
  }-*/;

  public final native void setTitleY(String title) /*-{
    this.titleY = title;
  }-*/;
}