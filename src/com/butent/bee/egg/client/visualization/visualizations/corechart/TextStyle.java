package com.butent.bee.egg.client.visualization.visualizations.corechart;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.egg.client.ajaxloader.Properties;

public class TextStyle extends Properties {
  public static TextStyle create() {
    return JavaScriptObject.createObject().cast();
  }

  protected TextStyle() {
  }

  public final native void setColor(String color) /*-{
    this.color = color;
  }-*/;

  public final native void setFontName(String fontName) /*-{
    this.fontName = fontName;
  }-*/;

  public final native void setFontSize(int fontSize) /*-{
    this.fontSize = fontSize;
  }-*/;
}
