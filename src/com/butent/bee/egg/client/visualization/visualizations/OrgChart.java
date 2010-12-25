package com.butent.bee.egg.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Element;

import com.butent.bee.egg.client.visualization.AbstractDataTable;
import com.butent.bee.egg.client.visualization.AbstractDrawOptions;
import com.butent.bee.egg.client.visualization.Selectable;
import com.butent.bee.egg.client.visualization.Selection;
import com.butent.bee.egg.client.visualization.events.CollapseHandler;
import com.butent.bee.egg.client.visualization.events.Handler;
import com.butent.bee.egg.client.visualization.events.OnMouseOutHandler;
import com.butent.bee.egg.client.visualization.events.OnMouseOverHandler;
import com.butent.bee.egg.client.visualization.events.ReadyHandler;
import com.butent.bee.egg.client.visualization.events.SelectHandler;

public class OrgChart extends Visualization<OrgChart.Options> implements Selectable {
  public static class Options extends AbstractDrawOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setAllowCollapse(boolean allow) /*-{
      this.allowCollapse = allow;
    }-*/;

    public final native void setAllowHtml(boolean allowHtml) /*-{
      this.allowHtml = allowHtml;
    }-*/;

    public final native void setNodeClass(String nodeClass) /*-{
      this.nodeClass = nodeClass;
    }-*/;

    public final native void setSelectedNodeClass(String selectedNodeClass) /*-{
      this.selectedNodeClass = selectedNodeClass;
    }-*/;

    public final void setSize(Size size) {
      setSize(size.name().toLowerCase());
    }

    private native void setSize(String size) /*-{
      this.size = size;
    }-*/;
  }

  public static enum Size {
    LARGE, MEDIUM, SMALL
  }

  public static final String PACKAGE = "orgchart";

  public OrgChart() {
    super();
  }

  public OrgChart(AbstractDataTable data, Options options) {
    super(data, options);
  }

  public final void addCollapseHandler(CollapseHandler handler) {
    Handler.addHandler(this, "collapse", handler);
  }

  public final void addOnMouseOutHandler(OnMouseOutHandler handler) {
    Handler.addHandler(this, "onmouseout", handler);
  }

  public final void addOnMouseOverHandler(OnMouseOverHandler handler) {
    Handler.addHandler(this, "onmouseover", handler);
  }

  public final void addReadyHandler(ReadyHandler handler) {
    Handler.addHandler(this, "ready", handler);
  }

  public final void addSelectHandler(SelectHandler handler) {
    Selection.addSelectHandler(this, handler);
  }

  public void collapse(int row, boolean collapsed) {
    this.collapse(getJso(), row, collapsed);
  }

  public JsArrayInteger getChildrenIndexes(int row) {
    return this.getChildrenIndexes(getJso(), row);
  }

  public JsArrayInteger getCollapsedNodes() {
    return this.getCollapsedNodes(getJso());
  }

  public final JsArray<Selection> getSelections() {
    return Selection.getSelections(this);
  }

  public final void setSelections(JsArray<Selection> sel) {
    Selection.setSelections(this, sel);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.OrgChart(parent);
  }-*/;

  private native void collapse(JavaScriptObject jso, int row, boolean collapsed) /*-{
    jso.collapse(row, collapsed);
  }-*/;

  private native JsArrayInteger getChildrenIndexes(JavaScriptObject jso, int row) /*-{
    return jso.getChildrenIndexes(row);
  }-*/;

  private native JsArrayInteger getCollapsedNodes(JavaScriptObject jso) /*-{
    return jso.getCollapsedNodes();
  }-*/;
}
