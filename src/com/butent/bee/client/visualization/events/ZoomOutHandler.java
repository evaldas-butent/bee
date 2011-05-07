package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Handles zooming out event in visualizations.
 */

public abstract class ZoomOutHandler extends Handler {

  /**
   * Occurs when a user calls for zoom out in visualizations.
   */

  public static class ZoomOutEvent {
  }

  public abstract void onZoomOut(ZoomOutEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onZoomOut(new ZoomOutEvent());
  }
}
