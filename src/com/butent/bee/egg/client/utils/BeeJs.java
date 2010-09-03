package com.butent.bee.egg.client.utils;

import com.google.gwt.core.client.JsArrayString;

public abstract class BeeJs {

  public static native int toInt(double from) /*-{
    return from;
  }-*/;

  public static native double toDouble(int from) /*-{
    return from;
  }-*/;

  public static native boolean isEmpty(String s) /*-{
    if (s == null || s == "")
    return true;
    else 
    return s.match(/\S/) == null;
  }-*/;

  public static native String toTime(double millis)
  /*-{
    if (millis > 0) {
    var d = new Date(millis);
    return d.toLocaleTimeString() + (d.getMilliseconds() / 1000).toString().substr(1);
    }
    else
    return "";
  }-*/;

  public static native String transform(double x, int dec) /*-{
    return x.toFixed(dec);
  }-*/;

  public static native String transform(int x) /*-{
    return x.toString();
  }-*/;

  public static native JsArrayString split(String src, String sep) /*-{
    if (src == null)
    return null;
    else
    return src.split(sep);
  }-*/;

  public static native String toSeconds(int millis) /*-{
    if (millis >= 0) {
    var z = millis / 1000;
    return z.toFixed(3);
    }
    else
    return "";
  }-*/;

  public static native String randomName(String pfx) /*-{
    if (pfx == null)
    return String(Math.random()).substr(2);
    else
    return pfx + String(Math.random()).substr(2);
  }-*/;

  public static native double currentTimeMillis() /*-{
    return (new Date()).getTime();
  }-*/;

}
