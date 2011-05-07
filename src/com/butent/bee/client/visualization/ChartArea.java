package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Creates chart area JavaScript object and sets it's parameters.
 */

public class ChartArea extends Properties {
  public static ChartArea create() {
    return JavaScriptObject.createObject().cast();
  }

  protected ChartArea() {
  }

  public final native void setHeight(String height) /*-{
    this.height = height;
  }-*/;

  public final native void setHeight(double height) /*-{
    this.height = height;
  }-*/;

  public final native void setLeft(double left) /*-{
    this.left = left;
  }-*/;

  public final native void setLeft(String left) /*-{
    this.left = left;
  }-*/;

  public final native void setTop(double top) /*-{
    this.top = top;
  }-*/;

  public final native void setTop(String top) /*-{
    this.top = top;
  }-*/;

  public final native void setWidth(double width) /*-{
    this.width = width;
  }-*/;

  public final native void setWidth(String width) /*-{
    this.width = width;
  }-*/;
}
