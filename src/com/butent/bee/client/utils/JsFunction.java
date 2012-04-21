package com.butent.bee.client.utils;

import com.google.gwt.core.client.JavaScriptObject;

public class JsFunction extends JavaScriptObject {

  public static native JsFunction create(String body) /*-{
    return new Function(body);
  }-*/;

  public static native JsFunction create(String params, String body) /*-{
    return new Function(params, body);
  }-*/;

  protected JsFunction() {
  }

  public final native void apply(JavaScriptObject thisArg) /*-{
    this.apply(thisArg);
  }-*/;

  public final native void call(JavaScriptObject thisArg) /*-{
    this.call(thisArg);
  }-*/;
}
