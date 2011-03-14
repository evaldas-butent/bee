package com.butent.bee.client.language;

import com.google.gwt.core.client.JavaScriptObject;

public class Error extends JavaScriptObject {

  protected Error() {
  }

  public final native int getCode() /*-{
    return this.code;
  }-*/;

  public final native String getMessage() /*-{
    return this.message;
  }-*/;
}
