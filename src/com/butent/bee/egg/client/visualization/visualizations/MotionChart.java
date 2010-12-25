package com.butent.bee.egg.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

import com.butent.bee.egg.client.visualization.AbstractDataTable;
import com.butent.bee.egg.client.visualization.AbstractDrawOptions;
import com.butent.bee.egg.client.visualization.events.Handler;
import com.butent.bee.egg.client.visualization.events.ReadyHandler;
import com.butent.bee.egg.client.visualization.events.StateChangeHandler;

public class MotionChart extends Visualization<MotionChart.Options> {
  public static class Options extends AbstractDrawOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setHeight(int height) /*-{
      this.height = height;
    }-*/;

    public final native void setShowAdvancedPanel(boolean showAdvancedPanel) /*-{
      this.showAdvancedPanel = showAdvancedPanel;
    }-*/;

    public final native void setShowChartButtons(boolean showChartButtons) /*-{
      this.showChartButtons = showChartButtons;
    }-*/;

    public final native void setShowHeader(boolean showHeader) /*-{
      this.showHeader = showHeader;
    }-*/;

    public final native void setShowSelectListComponent(
        boolean showSelectListComponent) /*-{
      this.showSelectListComponent = showSelectListComponent;
    }-*/;

    public final native void setShowSidePanel(boolean showSidePanel) /*-{
      this.showSidePanel = showSidePanel;
    }-*/;

    public final native void setShowXMetricPicker(boolean showXMetricPicker) /*-{
      this.showXMetricPicker = showXMetricPicker;
    }-*/;

    public final native void setShowXScalePicker(boolean showXScalePicker) /*-{
      this.showXScalePicker = showXScalePicker;
    }-*/;

    public final native void setShowYMetricPicker(boolean showYMetricPicker) /*-{
      this.showYMetricPicker = showYMetricPicker;
    }-*/;

    public final native void setShowYScalePicker(boolean showYScalePicker) /*-{
      this.showYScalePicker = showYScalePicker;
    }-*/;

    public final void setSize(int width, int height) {
      setWidth(width);
      setHeight(height);
    }

    public final native void setState(String state) /*-{
      this.state = state;
    }-*/;

    public final native void setWidth(int width) /*-{
      this.width = width;
    }-*/;
  }

  public static final String PACKAGE = "motionchart";

  public MotionChart() {
    super();
  }

  public MotionChart(AbstractDataTable data, Options options) {
    super(data, options);
  }

  public final void addReadyHandler(ReadyHandler handler) {
    Handler.addHandler(this, "ready", handler);
  }

  public final void addStateChangeHandler(StateChangeHandler handler) {
    Handler.addHandler(this, "statechange", handler);
  }

  public final native String getState() /*-{
    var jso = this.@com.butent.bee.egg.client.visualization.visualizations.Visualization::getJso()();
    if (jso.getState) {
      return jso.getState();
    }
    return null;
  }-*/;

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.MotionChart(parent);
  }-*/;
}
