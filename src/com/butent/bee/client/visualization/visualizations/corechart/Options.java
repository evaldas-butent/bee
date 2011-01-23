package com.butent.bee.client.visualization.visualizations.corechart;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.client.ajaxloader.ArrayHelper;
import com.butent.bee.client.visualization.AbstractDrawOptions;
import com.butent.bee.client.visualization.ChartArea;
import com.butent.bee.client.visualization.Color;
import com.butent.bee.client.visualization.LegendPosition;
import com.butent.bee.client.visualization.visualizations.corechart.CoreChart.Type;

public class Options extends AbstractDrawOptions {
  public static Options create() {
    return JavaScriptObject.createObject().cast();
  }

  protected Options() {
  }

  public final native void setAxisTitlesPosition(String position) /*-{
    this.axisTitlesPosition = position;
  }-*/;

  public final native void setBackgroundColor(String color) /*-{
    this.backgroundColor = color;
  }-*/;

  public final native void setBackgroundColor(Color color) /*-{
    this.backgroundColor = color;
  }-*/;
  
  public final native void setChartArea(ChartArea chartArea) /*-{
    this.chartArea = chartArea;
  }-*/;
  
  public final native void setChartAreaOptions(ChartAreaOptions options) /*-{
    this.chartArea = options;
  }-*/;

  public final native void setColors(JsArrayString colors) /*-{
    this.colors = colors;
  }-*/;

  public final void setColors(String... colors) {
    setColors(ArrayHelper.toJsArrayString(colors));
  }

  public final native void setCurveType(String type) /*-{
    this.curveType = type;
  }-*/;

  public final native void setFontName(String name) /*-{
    this.fontName = name;
  }-*/;

  public final native void setFontSize(double fontSize) /*-{
    this.fontSize = fontSize;
  }-*/;

  public final native void setGridlineColor(String color) /*-{
    this.gridlineColor = color;
  }-*/;
  
  public final native void setHAxisOptions(AxisOptions options) /*-{
    this.hAxis = options;
  }-*/;

  public final native void setHeight(int height) /*-{
    this.height = height;
  }-*/;

  public final native void setIsStacked(boolean isStacked) /*-{
    this.isStacked = isStacked;
  }-*/;

  public final void setLegend(LegendPosition position) {
    setLegend(position.toString().toLowerCase());
  }

  public final native void setLegend(String legend) /*-{
    this.legend = legend;
  }-*/;

  public final native void setLegendTextStyle(TextStyle style) /*-{
    this.legendTextStyle = style;
  }-*/;

  public final native void setLineWidth(int width) /*-{
    this.lineWidth = width;
  }-*/;

  public final native void setPointSize(int size) /*-{
    this.pointSize = size;
  }-*/;

  public final native void setReverseCategories(boolean reverseCategories) /*-{
    this.reverseCategories = reverseCategories;
  }-*/;

  public final native void setTitle(String title) /*-{
    this.title = title;
  }-*/;

  public final native void setTitleTextStyle(TextStyle style) /*-{
    this.titleTextStyle = style;
  }-*/;

  public final native void setTooltipTextStyle(TextStyle style) /*-{
    this.tooltipTextStyle = style;
  }-*/;

  public final native void setType(String type) /*-{
    this.type = type;
  }-*/;

  public final void setType(Type type) {
    setType(type.name().toLowerCase());
  }

  public final native void setVAxisOptions(AxisOptions options) /*-{
    this.vAxis = options;
  }-*/;

  public final native void setWidth(int width) /*-{
    this.width = width;
  }-*/;
}
