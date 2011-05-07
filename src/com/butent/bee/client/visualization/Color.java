package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Sets color related option values like fill, stroke and stroke size.
 */

public class Color extends Properties {
  public static Color create() {
    return JavaScriptObject.createObject().cast();
  }

  public static Color create(String fill, String stroke, int strokeSize) {
    Color result = create();
    result.setFill(fill);
    result.setStroke(stroke);
    result.setStrokeSize(strokeSize);
    return result;
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