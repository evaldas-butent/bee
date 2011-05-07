package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Handles events when elements change their status to "ready".
 */

public abstract class ReadyHandler extends Handler {

  /**
   * Occurs when elements change their status to "ready".
   */
  public static class ReadyEvent {
  }

  public abstract void onReady(ReadyEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onReady(new ReadyEvent());
  }
}
