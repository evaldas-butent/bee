package com.butent.bee.egg.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.AbstractDrawOptions;

public class Toolbar extends Widget {

  public static final String PACKAGE = "default";

  private JsArray<Component> components;
  private DivElement div;

  public enum Type {
    HTMLCODE("htmlcode"), CSV("csv"), HTML("html"), IGOOGLE("igoogle");

    private String typeCode;

    Type(String typeCode) {
      this.typeCode = typeCode;
    }

    public String getTypeCode() {
      return typeCode;
    }
  }

  public static class Component extends AbstractDrawOptions {
    public static Component create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Component() {
    }

    public final native void setDataSource(String dataSource) /*-{
      this.datasource = dataSource;
    }-*/;

    public final native void setGadget(String gadget) /*-{
      this.gadget = gadget;
    }-*/;

    public final void setType(Type type) {
      setType(type.typeCode);
    }

    public final native void setUserprefs(String userprefs) /*-{
      this.userprefs = userprefs;
    }-*/;

    private native void setType(String type) /*-{
      this.type = type;
    }-*/;
  }

  public Toolbar() {
    super();
    div = Document.get().createDivElement();
    setElement(div);
    setStyleName("viz-toolbar");
    components = createComponents();
  }

  public void addComponent(Component value) {
    components.set(components.length(), value);
  }

  @Override
  protected void onLoad() {
    nativeDraw(div, components);
  }

  private native JsArray<Component> createComponents() /*-{
    return [];
  }-*/;

  private native void nativeDraw(Element elem, JsArray<Component> comps) /*-{
    $wnd.google.visualization.drawToolbar(elem, comps);
  }-*/;
}
