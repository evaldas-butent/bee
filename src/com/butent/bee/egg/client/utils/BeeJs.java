package com.butent.bee.egg.client.utils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public abstract class BeeJs {

  public static native double currentTimeMillis() /*-{
    return (new Date()).getTime();
  }-*/;

  public static native JavaScriptObject eval(String xpr) /*-{
    try {
    return eval(xpr);
    }
    catch (err) {
    return err;
    }
  }-*/;

  public static native String evalToString(String xpr) /*-{
    try {
    var z = eval(xpr);
    if (typeof(z) == "object") {
    var s = "";
    var v = "";
    for (var p in z) {
    if (s.length > 0) {
    s = s + "; ";
    }
    try {
    v = String(z[p]);
    }
    catch (err) {
    v = "ERROR " + String(err);
    }
    s = s + p + '=' + v;
    }
    return s;
    }
    else {
    return String(z);
    }
    }
    catch (err) {
    return err.toString();
    }
  }-*/;

  public static native JsArrayString getFunctions(JavaScriptObject obj,
      String pattern) /*-{
    var arr = new Array();
    var i = 0;
    var v = null;
    var tp = null;
    var ok = true;

    for (var p in obj) {
    try {
    v = obj[p];
    tp = typeof(v);
    ok = true;
    }
    catch (err) {
    ok = false;
    }

    if (! ok) {
    continue;
    }
    if (tp != "function") {
    continue;
    }
    if (pattern != null && pattern.length > 0 && p.search(new RegExp(pattern, "i")) < 0) {
    continue;
    }

    arr[i] = p;
    i++; 
    }

    return arr;
  }-*/;

  public static native JsArrayString getProperties(JavaScriptObject obj,
      String pattern) /*-{
    var arr = new Array();
    var i = 0;
    var v = null;
    var tp = null;
    var ok = true;

    for (var p in obj) {
    try {
    v = obj[p];
    tp = typeof(v);
    ok = true;
    }
    catch (err) {
    arr[i] = p;
    arr[i + 1] = "error";
    arr[i + 2] = String(err);
    ok = false;
    }

    if (! ok) {
    continue;
    }
    if (tp == "function") {
    continue;
    }

    if (pattern != null && pattern.length > 0 && p.search(new RegExp(pattern, "i")) < 0) {
    continue;
    }

    arr[i] = p;
    try {
    arr[i + 1] = typeof(obj[p]);
    arr[i + 2] = String(obj[p]);
    }
    catch (err) {
    arr[i + 1] = "error";
    arr[i + 2] = String(err);
    }

    i += 3; 
    }

    return arr;
  }-*/;

  public static native JsArrayString getType(JavaScriptObject obj) /*-{
    return typeof(obj);
  }-*/;

  public static native boolean isEmpty(JavaScriptObject obj) /*-{
    if (obj == undefined || obj == null) {
    return true;
    }
    else if (obj instanceof Array) {
    return obj.length == 0;
    }
    else {
    return String(obj).length == 0;
    }
  }-*/;

  public static native boolean isEmpty(String s) /*-{
    if (s == null || s == "")
    return true;
    else 
    return s.match(/\S/) == null;
  }-*/;

  public static native String randomName(String pfx) /*-{
    if (pfx == null)
    return String(Math.random()).substr(2);
    else
    return pfx + String(Math.random()).substr(2);
  }-*/;

  public static native JsArrayString split(String src, String sep) /*-{
    if (src == null)
    return null;
    else
    return src.split(sep);
  }-*/;

  public static native double toDouble(int from) /*-{
    return from;
  }-*/;

  public static native int toInt(double from) /*-{
    return from;
  }-*/;

  public static native String toSeconds(int millis) /*-{
    if (millis >= 0) {
    var z = millis / 1000;
    return z.toFixed(3);
    }
    else
    return "";
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

}
