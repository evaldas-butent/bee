package com.butent.bee.egg.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

import com.butent.bee.egg.client.visualization.AbstractDataTable;
import com.butent.bee.egg.client.visualization.CommonChartOptions;

public class ImagePieChart extends Visualization<ImagePieChart.Options> {
  public static class Options extends CommonChartOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setColor(String color) /*-{
      this.color = color;
    }-*/;

    public final native void setIs3D(boolean is3D) /*-{
      this.is3D = is3D;
    }-*/;

    public final native void setLabels(String labels) /*-{
      this.labels = labels;
    }-*/;
  }

  public static final String PACKAGE = "imagepiechart";

  public ImagePieChart() {
    super();
  }

  public ImagePieChart(AbstractDataTable data, Options options) {
    super(data, options);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.ImagePieChart(parent);
  }-*/;
}
