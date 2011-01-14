package com.butent.bee.egg.client.ajaxloader;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;

import java.util.Date;

public class Properties extends JavaScriptObject {
  @SuppressWarnings("serial")
  public static class TypeException extends Exception {
    private TypeException(String key, String expected, String actual) {
      super("Properties.get" + expected + "(" + key + 
          ") failed.  Unexpected type : " + actual + ".");
    }
    
    private TypeException() {
    }
  }
  
  public static Properties create() {
    return JavaScriptObject.createObject().cast();
  }
  
  protected Properties() {
  }

  public final Boolean getBoolean(String key) throws TypeException {
    if (containsKey(key)) {
      String type = typeof(key);
      if (type.equals("boolean")) {
        return nativeGetBoolean(key);
      } else {
        throw new TypeException(key, "Boolean", type);
      }
    } else {
      return null;
    }
  }

  public final Date getDate(String key) throws JavaScriptException, TypeException {
    return JsDate.toJava((JsDate) getObject(key));
  }

  public final Double getNumber(String key) throws TypeException {
    if (containsKey(key)) {
      String type = typeof(key);
      if (type.equals("number")) {
        return nativeGetNumber(key);
      } else {
        throw new TypeException(key, "Number", type);
      }
    } else {
      return null;
    }
  }

  public final JavaScriptObject getObject(String key) throws TypeException {
    if (containsKey(key)) {
      String type = typeof(key);
      if (type.equals("object")) {
        return nativeGetObject(key);
      } else {
        throw new TypeException(key, "Object", type);
      }
    } else {
      return null;
    }
  }

  public final String getString(String key) throws TypeException {
    if (containsKey(key)) {
      String type = typeof(key);
      if (type.equals("string")) {
        return nativeGetString(key);
      } else {
        throw new TypeException(key, "String", type);
      }
    } else {
      return null;
    }
  }
  
  public final native void remove(String key) /*-{
    delete this[key];
  }-*/;

  public final void set(String key, Boolean value) {
    if (value == null) {
      remove(key);
    } else {
      setBoolean(key, value);
    }
  }

  public final void set(String key, Date value) {
    set(key, JsDate.toJs(value));
  }

  public final void set(String key, Double value) {
    if (value == null) {
      remove(key);
    } else {
      setNumber(key, value);
    }
  }

  public final native void set(String key, JavaScriptObject value) /*-{
    this[key] = value;
  }-*/;

  public final native void set(String key, String value) /*-{
    this[key] = value;
  }-*/;
  
  public final native String typeof(String key) /*-{
    return typeof this[key];
  }-*/;
  
  private native boolean containsKey(String key) /*-{
    return this[key] != null;
  }-*/;

  private native boolean nativeGetBoolean(String key) /*-{
    return this[key];
  }-*/;

  private native double nativeGetNumber(String key) /*-{
    return this[key];
  }-*/;

  private native JavaScriptObject nativeGetObject(String key) /*-{
    return this[key];
  }-*/;
  
  private native String nativeGetString(String key) /*-{
    return this[key];
  }-*/;
  
  private native void setBoolean(String key, boolean value) /*-{
    this[key] = value;
  }-*/;
  
  private native void setNumber(String key, double value) /*-{
    this[key] = value;
  }-*/;
}
