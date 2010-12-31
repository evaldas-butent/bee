package com.butent.bee.egg.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.AbstractDataTable;
import com.butent.bee.egg.client.visualization.AbstractDrawOptions;

public abstract class Visualization<OptionsType extends AbstractDrawOptions> extends Widget {
  public static Element createDiv(int width, int height) {
    Element result = DOM.createDiv();
    setSize(result, width, height);
    return result;
  }

  public static void setSize(Element div, int width, int height) {
    div.getStyle().setPropertyPx("width", width);
    div.getStyle().setPropertyPx("height", height);
  }

  private AbstractDataTable dataTable;
  private OptionsType options;
  private JavaScriptObject jso;

  public Visualization() {
    Element div = DOM.createDiv();
    jso = createJso(div);
    setElement(div);
    setStyleName("viz-container");
  }

  public Visualization(AbstractDataTable data, OptionsType options) {
    this();
    this.options = options;
    this.dataTable = data;
  }

  public final native void draw(AbstractDataTable data) /*-{
    this.@com.butent.bee.egg.client.visualization.visualizations.Visualization::jso.draw(data, {});
  }-*/;

  public final native void draw(AbstractDataTable data, OptionsType opt) /*-{
    this.@com.butent.bee.egg.client.visualization.visualizations.Visualization::jso.draw(data, opt);
  }-*/;

  public JavaScriptObject getJso() {
    return jso;
  }

  protected abstract JavaScriptObject createJso(Element div);

  @Override
  protected void onLoad() {
    if (dataTable != null && options != null) {
      draw(dataTable, options);
      dataTable = null;
      options = null;
    }
  }
}
