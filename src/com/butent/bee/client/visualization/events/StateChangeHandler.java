package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Handles change of state event in visualizations.
 */

public abstract class StateChangeHandler extends Handler {

  /**
   * Occurs when change of state happens in visualizations.
   */

  public static class StateChangeEvent {
  }

  public abstract void onStateChange(StateChangeEvent event);

  @Override
  protected void onEvent(Properties event) {
    onStateChange(new StateChangeEvent());
  }
}