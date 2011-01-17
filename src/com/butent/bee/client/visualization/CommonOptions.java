package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.client.ajaxloader.ArrayHelper;

public class CommonOptions extends AbstractDrawOptions {
  public static CommonOptions create() {
    return JavaScriptObject.createObject().cast();
  }

  protected CommonOptions() {
  }

  public final native void setBackgroundColor(Color color) /*-{
    this.backgroundColor = color;
  }-*/;

  public final native void setBackgroundColor(String color) /*-{
    this.backgroundColor = color;
  }-*/;

  public final native void setBorderColor(Color color) /*-{
    this.borderColor = color;
  }-*/;

  public final native void setBorderColor(String color) /*-{
    this.borderColor = color;
  }-*/;

  public final native void setColors(JsArrayString colors) /*-{
    this.colors = colors;
  }-*/;

  public final native void setColors(JsArray<Color3D> colors) /*-{
    this.colors = colors;
  }-*/;

  public final void setColors(String... colors) {
    setColors(ArrayHelper.toJsArrayString(colors));
  }

  public final void setColors(Color3D... colors) {
    setColors(ArrayHelper.toJsArray(colors));
  }

  public final native void setEnableTooltip(boolean enableTooltip) /*-{
    this.enableTooltip = enableTooltip;
  }-*/;

  public final native void setFocusBorderColor(Color color) /*-{
    this.focusBorderColor = color;
  }-*/;

  public final native void setFocusBorderColor(String color) /*-{
    this.focusBorderColor = color;
  }-*/;

  public final native void setHeight(int height) /*-{
    this.height = height;
  }-*/;

  public final void setLegend(LegendPosition position) {
    setLegend(position.toString());
  }

  public final native void setLegendBackgroundColor(Color color) /*-{
    this.legendBackgroundColor = color;
  }-*/;

  public final native void setLegendBackgroundColor(String color) /*-{
    this.legendBackgroundColor = color;
  }-*/;

  public final native void setLegendFontSize(double fontSize) /*-{
    this.legendFontSize = fontSize;
  }-*/;

  public final native void setLegendTextColor(Color color) /*-{
    this.legendTextColor = color;
  }-*/;

  public final native void setLegendTextColor(String color) /*-{
    this.legendTextColor = color;
  }-*/;

  public final void setSize(int width, int height) {
    setWidth(width);
    setHeight(height);
  }

  public final native void setTitle(String title) /*-{
    this.title = title;
  }-*/;

  public final native void setTitleColor(Color color) /*-{
    this.titleColor = color;
  }-*/;

  public final native void setTitleColor(String color) /*-{
    this.titleColor = color;
  }-*/;

  public final native void setTitleFontSize(double fontSize) /*-{
    this.titleFontSize = fontSize;
  }-*/;

  public final native void setTooltipHeight(int tooltipHeight) /*-{
    this.tooltipHeight = tooltipHeight;
  }-*/;

  public final native void setTooltipWidth(int tooltipWidth) /*-{
    this.tooltipWidth = tooltipWidth;
  }-*/;

  public final native void setWidth(int width) /*-{
    this.width = width;
  }-*/;

  private native void setLegend(String legend) /*-{
    this.legend = legend;
  }-*/;
}