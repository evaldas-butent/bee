package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.CommonChartOptions;

/**
 * Implements image area chart visualization.
 */

public class ImageAreaChart extends Visualization<ImageAreaChart.Options> {

  /**
   * Sets option values for image area chart visualization.
   */

  public static class Options extends CommonChartOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

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

  public static final String PACKAGE = "imageareachart";

  public ImageAreaChart() {
    super();
  }

  public ImageAreaChart(AbstractDataTable data, Options options) {
    super(data, options);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.ImageAreaChart(parent);
  }-*/;
}
