package com.butent.bee.egg.client.ajaxloader;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.Date;

public class JsDate extends JavaScriptObject {  
  public static native JsDate create() /*-{
    var result = new $wnd.Date();
    result.constructor = $wnd.Date;
    return result;
  }-*/;
  
  public static native JsDate create(double time) /*-{
    var result = new $wnd.Date(time);
    result.constructor = $wnd.Date;
    return result;
  }-*/;

  public static native boolean isDate(JavaScriptObject js) /*-{
    var result = false;
    if (js != null) {
      if (typeof js.getTime == 'function') {
        var time = js.getTime();
        if (typeof time == 'number') {
          result = true;
        }
      }
    }
    return result;
  }-*/;
  
  public static Date toJava(JsDate js) {
    return js == null ? null : new Date(js.getTime());
  }
  
  public static JsDate toJs(Date java) {
    return java == null ? null : create(java.getTime());
  }  
  
  protected JsDate() {
  }

  public final long getTime() {
    return (long) doubleTime();
  }
  
  private native double doubleTime() /*-{
    return this.getTime();
  }-*/;
}
