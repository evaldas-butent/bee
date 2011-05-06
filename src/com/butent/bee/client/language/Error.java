package com.butent.bee.client.language;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Processes any translation errors that occur.
 */

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
