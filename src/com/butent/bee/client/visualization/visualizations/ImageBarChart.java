package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.CommonChartOptions;

/**
 * Implements image bar chart visualization.
 */

public class ImageBarChart extends Visualization<ImageBarChart.Options> {

  /**
   * Sets option values for image bar chart visualization.
   */

  public static class Options extends CommonChartOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setIsStacked(boolean isStacked) /*-{
      this.isStacked = isStacked;
    }-*/;

    public final native void setIsVertical(boolean isVertical) /*-{
      this.isVertical = isVertical;
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

  public static final String PACKAGE = "imagebarchart";

  public ImageBarChart() {
    super();
  }

  public ImageBarChart(AbstractDataTable data, Options options) {
    super(data, options);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.ImageBarChart(parent);
  }-*/;
}
