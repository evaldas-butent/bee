package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;
import com.butent.bee.client.ajaxloader.Properties.TypeException;

public abstract class StateChangeHandler extends Handler {
  public static class StateChangeEvent {
  }

  public abstract void onStateChange(StateChangeEvent event);

  @Override
  protected void onEvent(Properties event) throws TypeException {
    onStateChange(new StateChangeEvent());
  }
}