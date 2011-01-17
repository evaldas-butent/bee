package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

public abstract class ReadyHandler extends Handler {
  public static class ReadyEvent {
  }

  public abstract void onReady(ReadyEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onReady(new ReadyEvent());
  }
}
