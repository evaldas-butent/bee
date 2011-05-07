package com.butent.bee.client.visualization.formatters;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.visualization.DataTable;

/**
 * Manages formatting for bars in visualizations.
 */

public class BarFormat extends JavaScriptObject {
  /**
   * Enlists possible base colors for color mixing.
   */
  public static enum Color {
    RED, GREEN, BLUE;

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }
  /**
   * Sets options for bar formatting.
   */
  public static class Options extends JavaScriptObject {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setBase(double base) /*-{
      this.base = base;
    }-*/;

    public final void setColorNegative(Color color) {
      setColorNegative(color.toString());
    }

    public final void setColorPositive(Color color) {
      setColorPositive(color.toString());
    }

    public final native void setMax(double max) /*-{
      this.max = max;
    }-*/;

    public final native void setMin(double min) /*-{
      this.min = min;
    }-*/;

    public final native void setShowValue(boolean show) /*-{
      this.showValue = show;
    }-*/;

    public final native void setWidth(int width) /*-{
      this.width = width;
    }-*/;

    private native void setColorNegative(String color) /*-{
      this.colorNegative = color;
    }-*/;

    private native void setColorPositive(String color) /*-{
      this.colorNegative = color;
    }-*/;
  }

  public static native BarFormat create(Options options) /*-{
    return new $wnd.google.visualization.BarFormat(options);
  }-*/;

  protected BarFormat() {
  }

  public final native void format(DataTable data, int columnIndex) /*-{
    this.format(data, columnIndex);
  }-*/;
}
