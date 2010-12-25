package com.butent.bee.egg.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

import com.butent.bee.egg.client.visualization.AbstractDataTable;
import com.butent.bee.egg.client.visualization.CommonChartOptions;

public class ImageSparklineChart extends Visualization<ImageSparklineChart.Options> {
  public static class Options extends CommonChartOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setColor(String color) /*-{
      this.color = color;
    }-*/;

    public final native void setFill(boolean fill) /*-{
      this.fill = fill;
    }-*/;

    public final native void setLabelPosition(String labelPosition) /*-{
      this.labelPosition = labelPosition;
    }-*/;

    public final native void setLayout(String layout) /*-{
      this.layout = layout;
    }-*/;

    public final native void setShowAxisLines(boolean showAxisLines) /*-{
      this.showAxisLines = showAxisLines;
    }-*/;

    public final native void setShowValueLabels(boolean showValueLabels) /*-{
      this.showValueLabels = showValueLabels;
    }-*/;
  }

  public static final String PACKAGE = "imagesparkline";

  public ImageSparklineChart() {
    super();
  }

  public ImageSparklineChart(AbstractDataTable data, Options options) {
    super(data, options);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.ImageSparkLine(parent);
  }-*/;
}
