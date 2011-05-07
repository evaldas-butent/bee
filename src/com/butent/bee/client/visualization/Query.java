package com.butent.bee.client.visualization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Enables creating queries to get data for visualizations.
 */

public class Query extends JavaScriptObject {

  /**
   * Requires to have a {@code onResponse} method.
   */
  public interface Callback {
    void onResponse(QueryResponse response);
  }

  /**
   * Sets option values for visualization data queries.
   */

  public static class Options extends Properties {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setMakeRequestParams(JavaScriptObject params) /*-{
      this.makeRequestParams = params;
    }-*/;

    public final void setSendMethod(SendMethod sendMethod) {
      setSendMethod(sendMethod.toString());
    }

    private native void setSendMethod(String sendMethod) /*-{
      this.sendMethod = sendMethod;
    }-*/;
  }

  /**
   * Contains a list of possible visualization query methods.
   */

  public static enum SendMethod {
    XHR("xhr"), SCRIPT_INJECTION("scriptInjection"), MAKE_REQUEST("makeRequest"), AUTO("auto");

    private String strValue;

    private SendMethod(String value) {
      strValue = value;
    }

    @Override
    public String toString() {
      return strValue;
    }
  }

  public static final native Query create(String dataSource) /*-{
    return new $wnd.google.visualization.Query(dataSource);
  }-*/;

  public static final native Query create(String dataSource, Options options) /*-{
    return new $wnd.google.visualization.Query(dataSource, options);
  }-*/;

  private static void fireAndCatch(UncaughtExceptionHandler handler,
      Callback callback, QueryResponse response) {
    try {
      fireImpl(callback, response);
    } catch (Throwable e) {
      handler.onUncaughtException(e);
    }
  }

  private static void fireImpl(Callback callback, QueryResponse response) {
    callback.onResponse(response);
  }

  private static void onResponseCallback(Callback callback,
      QueryResponse response) {
    UncaughtExceptionHandler handler = GWT.getUncaughtExceptionHandler();
    if (handler != null) {
      fireAndCatch(handler, callback, response);
    } else {
      fireImpl(callback, response);
    }
  }

  protected Query() {
  }

  public final native void send(Callback callback) /*-{
    this
        .send(function(c) {
          @com.butent.bee.client.visualization.Query::onResponseCallback(Lcom/butent/bee/client/visualization/Query$Callback;Lcom/butent/bee/client/visualization/QueryResponse;)(callback, c);
        });
  }-*/;

  public final native void setQuery(String query) /*-{
    this.setQuery(query);
  }-*/;

  public final native void setRefreshInterval(int timeInSeconds) /*-{
    this.setRefreshInterval(timeInSeconds);
  }-*/;

  public final native void setTimeout(int seconds) /*-{
    this.setTimeout(seconds);
  }-*/;
}