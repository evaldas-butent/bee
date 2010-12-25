package com.butent.bee.egg.client.visualization.events;

import com.butent.bee.egg.client.ajaxloader.Properties;

public abstract class ZoomOutHandler extends Handler {
  public static class ZoomOutEvent {
  }

  public abstract void onZoomOut(ZoomOutEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onZoomOut(new ZoomOutEvent());
  }
}
