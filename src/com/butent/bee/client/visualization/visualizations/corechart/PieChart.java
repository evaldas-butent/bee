package com.butent.bee.client.visualization.visualizations.corechart;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.visualization.AbstractDataTable;

public class PieChart extends CoreChart {
  public static class PieOptions extends Options {
    public static PieOptions create() {
      return JavaScriptObject.createObject().cast();
    }

    protected PieOptions() {
    }

    public final native void set3D(boolean is3D) /*-{
      this.is3D = is3D;
    }-*/;

    public final native void setPieResidueSliceLabel(String label) /*-{
      this.pieResidueSliceLabel = label;
    }-*/;
    
    public final native void setPieSliceText(String text) /*-{
      this.pieSliceText = text;
    }-*/;

    public final native void setPieSliceTextStyle(TextStyle textStyle) /*-{
      this.pieSliceTextStyle = textStyle;
    }-*/;

    public final native void setSliceVisibilityThreshold(double angle) /*-{
      this.sliceVisibilityThreshold = angle;
    }-*/;
  }

  public static PieOptions createPieOptions() {
    return JavaScriptObject.createObject().cast();
  }

  public PieChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.PIE);
  }
}
