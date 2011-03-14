package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

public abstract class StateChangeHandler extends Handler {
  public static class StateChangeEvent {
  }

  public abstract void onStateChange(StateChangeEvent event);

  @Override
  protected void onEvent(Properties event) {
    onStateChange(new StateChangeEvent());
  }
}