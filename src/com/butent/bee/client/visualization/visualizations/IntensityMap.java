package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.ajaxloader.ArrayHelper;
import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.AbstractDrawOptions;
import com.butent.bee.client.visualization.Selectable;
import com.butent.bee.client.visualization.Selection;
import com.butent.bee.client.visualization.events.SelectHandler;

public class IntensityMap extends Visualization<IntensityMap.Options> implements Selectable {
  public static class Options extends AbstractDrawOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setColors(JsArrayString colors) /*-{
      this.colors = colors;
    }-*/;

    public final void setColors(String... colors) {
      setColors(ArrayHelper.toJsArrayString(colors));
    }

    public final native void setHeight(int height) /*-{
      this.height = height;
    }-*/;

    public final void setRegion(Region region) {
      setRegion(region.name().toLowerCase());
    }

    public final native void setShowOneTab(boolean show) /*-{
      this.showOneTab = show;
    }-*/;

    public final void setSize(int width, int height) {
      setWidth(width);
      setHeight(height);
    }

    public final native void setWidth(int width) /*-{
      this.width = width;
    }-*/;

    private native void setRegion(String region) /*-{
      this.region = region;
    }-*/;
  }

  public static enum Region {
    AFRICA, ASIA, EUROPE, MIDDLE_EAST, SOUTH_AMERICA, USA, WORLD
  }

  public static final String PACKAGE = "intensitymap";

  public IntensityMap() {
    super();
  }

  public IntensityMap(AbstractDataTable data, Options options) {
    super(data, options);
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
    return new $wnd.google.visualization.IntensityMap(parent);
  }-*/;
}
