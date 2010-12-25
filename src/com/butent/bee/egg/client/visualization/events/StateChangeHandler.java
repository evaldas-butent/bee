package com.butent.bee.egg.client.visualization.events;

import com.butent.bee.egg.client.ajaxloader.Properties;
import com.butent.bee.egg.client.ajaxloader.Properties.TypeException;

public abstract class StateChangeHandler extends Handler {
  public static class StateChangeEvent {
  }

  public abstract void onStateChange(StateChangeEvent event);

  @Override
  protected void onEvent(Properties event) throws TypeException {
    onStateChange(new StateChangeEvent());
  }
}