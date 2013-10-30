package com.butent.bee.client.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

public class ClientRect extends JavaScriptObject {
  
  public static native ClientRect createBounding(Element element) /*-{
    return element.getBoundingClientRect();
  }-*/;
  
  protected ClientRect() {
  }
  
  public final boolean contains(ClientRect other) {
    return other != null && other.getLeft() >= getLeft() && other.getRight() <= getRight()
        && other.getTop() >= getTop() && other.getBottom() <= getBottom();
  }
  
  public final native double getBottom() /*-{
    return this.bottom;
  }-*/;

  public final native double getHeight() /*-{
    return this.height;
  }-*/;

  public final native double getLeft() /*-{
    return this.left;
  }-*/;

  public final native double getRight() /*-{
    return this.right;
  }-*/;

  public final native double getTop() /*-{
    return this.top;
  }-*/;

  public final native double getWidth() /*-{
    return this.width;
  }-*/;
  
  public final boolean intersects(ClientRect other) {
    return other != null && other.getLeft() < getRight() && other.getRight() > getLeft()
        && other.getTop() < getBottom() && other.getBottom() > getTop();
  }
}
