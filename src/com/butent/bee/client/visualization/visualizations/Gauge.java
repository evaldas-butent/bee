package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.ajaxloader.ArrayHelper;
import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.AbstractDrawOptions;

public class Gauge extends Visualization<Gauge.Options> {
  public static class Options extends AbstractDrawOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setGaugeRange(int min, int max) /*-{
      this.min = min;
      this.max = max;
    }-*/;

    public final native void setGreenRange(int from, int to) /*-{
      this.greenFrom = from;
      this.greenTo = to;
    }-*/;

    public final native void setHeight(int height) /*-{
      this.height = height;
    }-*/;

    public final void setMajorTicks(String... labels) {
      setMajorTicks(ArrayHelper.toJsArrayString(labels));
    }

    public final native void setMinorTicks(int numberOfTicks) /*-{
      this.minorTicks = numberOfTicks;
     }-*/;

    public final native void setRedRange(int from, int to) /*-{
      this.redFrom = from;
      this.redTo = to;
    }-*/;

    public final void setSize(int width, int height) {
      setWidth(width);
      setHeight(height);
    }

    public final native void setWidth(int width) /*-{
      this.width = width;
    }-*/;

    public final native void setYellowRange(int from, int to) /*-{
      this.yellowFrom = from;
      this.yellowTo = to;
    }-*/;

    private native void setMajorTicks(JsArrayString labels) /*-{
      this.majorTicks = labels;
    }-*/;
  }

  public static final String PACKAGE = "gauge";

  public Gauge() {
    super();
  }

  public Gauge(AbstractDataTable data, Options options) {
    super(data, options);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.Gauge(parent);
  }-*/;
}
