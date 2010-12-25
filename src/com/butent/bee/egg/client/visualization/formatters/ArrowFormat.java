package com.butent.bee.egg.client.visualization.formatters;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.egg.client.visualization.DataTable;

public class ArrowFormat extends JavaScriptObject {  
  public static class Options extends JavaScriptObject {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }
    
    public final native void setBase(double base) /*-{
      this.base = base;
    }-*/;
  }
  
  public static native ArrowFormat create(Options options) /*-{
    return new $wnd.google.visualization.ArrowFormat(options);
  }-*/;

  protected ArrowFormat() {
  }
  
  public final native void format(DataTable data, int columnIndex) /*-{
    this.format(data, columnIndex);
  }-*/;
}
