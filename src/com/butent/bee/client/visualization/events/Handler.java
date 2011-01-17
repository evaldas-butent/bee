package com.butent.bee.client.visualization.events;

import com.google.gwt.core.client.GWT;

import com.butent.bee.client.ajaxloader.Properties;
import com.butent.bee.client.ajaxloader.Properties.TypeException;
import com.butent.bee.client.visualization.visualizations.Visualization;

public abstract class Handler {
  public static native void addHandler(Visualization<?> viz, String eventName,
      Handler handler) /*-{
    var jso = viz.@com.butent.bee.client.visualization.visualizations.Visualization::getJso()();
    var callback = function(event) {
    @com.butent.bee.client.visualization.events.Handler::onCallback(Lcom/butent/bee/client/visualization/events/Handler;Lcom/butent/bee/client/ajaxloader/Properties;)
    (handler, event);
    };
    $wnd.google.visualization.events.addListener(jso, eventName, callback);
  }-*/;

  private static void onCallback(final Handler handler, final Properties properties) {
    try {
      handler.onEvent(properties);
    } catch (Throwable x) {
      GWT.getUncaughtExceptionHandler().onUncaughtException(x);
    }
  }

  protected abstract void onEvent(Properties properties) throws TypeException;
}
