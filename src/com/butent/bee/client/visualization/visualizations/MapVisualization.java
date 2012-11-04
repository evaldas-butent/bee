package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.AbstractDrawOptions;
import com.butent.bee.client.visualization.Selectable;
import com.butent.bee.client.visualization.Selection;
import com.butent.bee.client.visualization.events.SelectHandler;

/**
 * Implements map type visualization.
 */

public class MapVisualization extends Visualization<MapVisualization.Options>
    implements Selectable {

  /**
   * Sets option values for map type visualization.
   */
  public static class Options extends AbstractDrawOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setEnableScrollWheel(boolean enable) /*-{
      this.enableScrollWheel = enable;
    }-*/;

    public final native void setLineColor(String color) /*-{
      this.lineColor = color;
    }-*/;

    public final native void setLineWidth(int width) /*-{
      this.lineWidth = width;
    }-*/;

    public final void setMapType(Type type) {
      setMapType(type.name().toLowerCase());
    }

    public final native void setShowLine(boolean show) /*-{
      this.showLine = show;
    }-*/;

    public final native void setShowTip(boolean show) /*-{
      this.showTip = show;
    }-*/;

    public final native void setZoomLevel(double zoomLevel) /*-{
      this.zoomLevel = zoomLevel;
    }-*/;

    private native void setMapType(String type) /*-{
      this.mapType = type;
    }-*/;
  }

  /**
   * Contains a list of possible map types.
   */

  public enum Type {
    HYBRID, NORMAL, SATELLITE
  }

  public static final String PACKAGE = "map";

  public MapVisualization(AbstractDataTable data, Options options, String width, String height) {
    super(data, options);
    setSize(width, height);
  }

  public MapVisualization(String width, String height) {
    super();
    setSize(width, height);
  }

  public final void addSelectHandler(SelectHandler handler) {
    Selection.addSelectHandler(this, handler);
  }

  public final JsArray<Selection> getSelections() {
    return Selection.getSelections(this);
  }

  public final void setSelections(JsArray<Selection> sel) {
    Selection.setSelections(this, sel);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.Map(parent);
  }-*/;
}
