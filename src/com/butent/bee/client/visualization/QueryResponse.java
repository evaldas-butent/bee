package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;

public class QueryResponse extends JavaScriptObject {
  protected QueryResponse() {
  }

  public final native DataTable getDataTable() /*-{
    return this.getDataTable();
  }-*/;

  public final native String getDetailedMessage() /*-{
    return this.getDetailedMessage();
  }-*/;

  public final native String getMessage() /*-{
    return this.getMessage();
  }-*/;

  public final native boolean hasWarning() /*-{
    return this.hasWarning();
  }-*/;

  public final native boolean isError() /*-{
    return this.isError();
  }-*/;
}