package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.ajaxloader.Properties;

public class Color extends Properties {
  public static Color create() {
    return JavaScriptObject.createObject().cast();
  }

  protected Color() {
  }

  public final native void setFill(String fill) /*-{
    this.fill = fill;
  }-*/;

  public final native void setStroke(String stroke) /*-{
    this.stroke = stroke;
  }-*/;

  public final native void setStrokeSize(int strokeSize) /*-{
    this.strokeSize = strokeSize;
  }-*/;
}