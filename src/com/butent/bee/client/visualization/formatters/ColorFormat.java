package com.butent.bee.client.visualization.formatters;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.visualization.DataTable;

public class ColorFormat extends JavaScriptObject {
  public static native ColorFormat create() /*-{
    return new $wnd.google.visualization.ColorFormat();
  }-*/;

  protected ColorFormat() {
  }

  public final native void addGradientRange(double from, double to,
      String color, String fromBgColor, String toBgColor) /*-{
    this.addGradientRange(from, to, color, fromBgColor, toBgColor);
  }-*/;

  public final native void addRange(double from, double to, String color, String bgcolor) /*-{
    this.addRange(from, to, color, bgcolor);
  }-*/;

  public final native void format(DataTable data, int columnIndex) /*-{
    this.format(data, columnIndex);
  }-*/;
}
