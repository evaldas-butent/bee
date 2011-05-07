package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;
import com.butent.bee.client.visualization.visualizations.Visualization;

/**
 * Loads events handlers in visualizations and requires from it's child classes to have methods for
 * callback and event processing.
 */

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
    handler.onEvent(properties);
  }

  protected abstract void onEvent(Properties properties);
}
