package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.ajaxloader.ArrayHelper;
import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.AbstractDrawOptions;
import com.butent.bee.client.visualization.Selectable;
import com.butent.bee.client.visualization.Selection;
import com.butent.bee.client.visualization.events.Handler;
import com.butent.bee.client.visualization.events.ReadyHandler;
import com.butent.bee.client.visualization.events.RegionClickHandler;
import com.butent.bee.client.visualization.events.SelectHandler;
import com.butent.bee.client.visualization.events.ZoomOutHandler;

public class GeoMap extends Visualization<GeoMap.Options> implements Selectable {
  public static enum DataMode { MARKERS, REGIONS }

  public static class Options extends AbstractDrawOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }
    
    public final void setColors(int... colors) {
      setColors(ArrayHelper.toJsArrayInteger(colors));
    }

    public final native void setColors(JsArrayInteger colors) /*-{
      this.colors = colors;
    }-*/;

    public final void setDataMode(DataMode mode) {
      setDataMode(mode.name().toLowerCase());
    }

    public final native void setHeight(int height) /*-{
      this.height = height + 'px';
    }-*/;

    public final native void setHeight(String height) /*-{
      this.height = height;
    }-*/;

    public final native void setRegion(String region) /*-{
      this.region = region;
    }-*/;

    public final native void setShowLegend(boolean show) /*-{
      this.showLegend = show;
    }-*/;

    public final native void setShowZoomOut(boolean show) /*-{
      this.showZoomOut = show;
    }-*/;

    public final void setSize(int width, int height) {
      setWidth(width);
      setHeight(height);
    }

    public final void setSize(String width, String height) {
      setWidth(width);
      setHeight(height);
    }

    public final native void setWidth(int width) /*-{
      this.width = width + 'px';
    }-*/;
    
    public final native void setWidth(String width) /*-{
      this.width = width;
    }-*/;
    
    public final native void setZoomOutLabel(String label) /*-{
      this.zoomOutLabel = label;
    }-*/;

    private native void setDataMode(String mode) /*-{
      this.dataMode = mode;
    }-*/;
  }

  public static final String PACKAGE = "geomap";

  public GeoMap() {
    super();
    setSize("100%", "100%");   
  }

  public GeoMap(AbstractDataTable data, Options options) {
    super(data, options);
    setSize("100%", "100%");
  }

  public void addReadyHandler(ReadyHandler handler) {
    Handler.addHandler(this, "drawingDone", handler);
  }

  public void addRegionClickHandler(RegionClickHandler handler) {
    Handler.addHandler(this, "regionClick", handler);
  }

  public final void addSelectHandler(SelectHandler handler) {
    Selection.addSelectHandler(this, handler);
  }

  public void addZoomOutHandler(ZoomOutHandler handler) {
    Handler.addHandler(this, "zoomOut", handler);
  }

  public final JsArray<Selection> getSelections() {
    return Selection.getSelections(this);
  }

  public final void setSelections(JsArray<Selection> sel) {
    Selection.setSelections(this, sel);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.GeoMap(parent);
  }-*/; 
}
