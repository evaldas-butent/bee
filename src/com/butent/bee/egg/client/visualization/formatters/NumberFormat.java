package com.butent.bee.egg.client.visualization.formatters;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.egg.client.visualization.DataTable;

public class NumberFormat extends JavaScriptObject {
  public static class Options extends JavaScriptObject {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setDecimalSymbol(String symbol) /*-{
      this.decimalSymbol = symbol;
    }-*/;

    public final native void setFractionDigits(int n) /*-{
      this.fractionDigits = n;
    }-*/;

    public final native void setNegativeColor(String color) /*-{
      this.negativeColor = color;
    }-*/;

    public final native void setNegativeParens(boolean parens) /*-{
      this.negativeParens = parens;
    }-*/;

    public final native void setPrefix(String prefix) /*-{
      this.prefix = prefix;
    }-*/;

    public final native void setSuffix(String suffix) /*-{
      this.suffix = suffix;
    }-*/;
  }

  public static native NumberFormat create(Options options) /*-{
    return new $wnd.google.visualization.NumberFormat(options);
  }-*/;

  protected NumberFormat() {
  }

  public final native void format(DataTable data, int columnIndex) /*-{
    this.format(data, columnIndex);
  }-*/;
}
