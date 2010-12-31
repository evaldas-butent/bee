package com.butent.bee.egg.client.visualization.visualizations.corechart;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.egg.client.visualization.AbstractDrawOptions;

public class ChartArea extends AbstractDrawOptions {
  public static ChartArea create() {
    return JavaScriptObject.createObject().cast();
  }

  protected ChartArea() {
  }

  public final native void setHeight(int height) /*-{
    this.height = height;
  }-*/;

  public final native void setHeightPct(int height) /*-{
    this.height = height + "%";
  }-*/;
  
  public final native void setLeft(int left) /*-{
    this.left = left;
  }-*/;
  
  public final native void setLeftPct(int left) /*-{
    this.left = left + "%";
  }-*/;
  
  public final native void setTop(int top) /*-{
    this.top = top;
  }-*/;
  
  public final native void setTopPct(int top) /*-{
    this.top = top + "%";
  }-*/;
  
  public final native void setWidth(int width) /*-{
    this.width = width;
  }-*/;

  public final native void setWidthPct(int width) /*-{
    this.width = width + "%";
  }-*/;
}
