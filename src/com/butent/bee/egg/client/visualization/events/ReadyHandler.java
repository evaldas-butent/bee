package com.butent.bee.egg.client.visualization.events;

import com.butent.bee.egg.client.ajaxloader.Properties;

public abstract class ReadyHandler extends Handler {
  public static class ReadyEvent {
  }

  public abstract void onReady(ReadyEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onReady(new ReadyEvent());
  }
}
