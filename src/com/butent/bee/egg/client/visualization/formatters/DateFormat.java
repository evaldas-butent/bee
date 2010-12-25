package com.butent.bee.egg.client.visualization.formatters;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.egg.client.visualization.DataTable;

public class DateFormat extends JavaScriptObject {
  public static enum FormatType { SHORT, MEDIUM, LONG; 
    
    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  public static class Options extends JavaScriptObject {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }
    
    public final void setPattern(FormatType pattern) {
      setFormatType(pattern.toString());
    }
    
    public final native void setPattern(String pattern) /*-{
      this.pattern = pattern;
    }-*/;
    
    public final native void setTimeZone(int zone) /*-{
      this.timeZone = zone;
    }-*/;
    
    private native void setFormatType(String type) /*-{
      this.formatType = type;
    }-*/;
  }
  
  public static native DateFormat create(Options options) /*-{
    return new $wnd.google.visualization.DateFormat(options);
  }-*/;

  protected DateFormat() {
  }
  
  public final native void format(DataTable data, int columnIndex) /*-{
    this.format(data, columnIndex);
  }-*/;
}
