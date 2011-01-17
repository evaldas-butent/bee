package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.CommonChartOptions;

public class ImageLineChart extends Visualization<ImageLineChart.Options> {
  public static class Options extends CommonChartOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setShowAxisLines(boolean showAxisLines) /*-{
      this.showAxisLines = showAxisLines;
    }-*/;

    public final native void setShowCategoryLabels(boolean showCategoryLabels) /*-{
      this.showCategoryLabels = showCategoryLabels;
    }-*/;

    public final native void setShowValueLabels(boolean showValueLabels) /*-{
      this.showValueLabels = showValueLabels;
    }-*/;

    public final native void setValueLabelsInterval(double valueLabelsInterval) /*-{
      this.valueLabelsInterval = valueLabelsInterval;
    }-*/;
  }

  public static final String PACKAGE = "imagelinechart";

  public ImageLineChart() {
    super();
  }

  public ImageLineChart(AbstractDataTable data, Options options) {
    super(data, options);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.ImageLineChart(parent);
  }-*/;
}
