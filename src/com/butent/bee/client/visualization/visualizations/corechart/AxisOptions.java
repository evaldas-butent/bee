package com.butent.bee.client.visualization.visualizations.corechart;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.visualization.AbstractDrawOptions;

public class AxisOptions extends AbstractDrawOptions {
  public static AxisOptions create() {
    return JavaScriptObject.createObject().cast();
  }

  protected AxisOptions() {
  }

  public final native void setBaseline(double baseline) /*-{
    this.baseline = baseline;
  }-*/;

  public final native void setBaselineColor(String baselineColor) /*-{
    this.baselineColor = baselineColor;
  }-*/;

  public final native void setDirection(int direction) /*-{
    this.direction = direction;
  }-*/;

  public final native void setIsLogScale(boolean isLogScale) /*-{
    this.logScale = isLogScale;
  }-*/;

  public final native void setMaxValue(double max) /*-{
    this.maxValue = max;
  }-*/;

  public final native void setMinValue(double min) /*-{
    this.minValue = min;
  }-*/;

  public final native void setTextStyle(TextStyle style) /*-{
    this.textStyle = style;
  }-*/;

  public final native void setTitle(String title) /*-{
    this.title = title;
  }-*/;

  public final native void setTitleTextStyle(TextStyle style) /*-{
    this.titleTextStyle = style;
  }-*/;
}
