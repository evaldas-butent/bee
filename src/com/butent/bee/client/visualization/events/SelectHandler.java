package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Handles user selection events.
 */

public abstract class SelectHandler extends Handler {

  /**
   * Occurs when a user selects certain parts of user interface components.
   */

  public static class SelectEvent {
  }

  public abstract void onSelect(SelectEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onSelect(new SelectEvent());
  }
}
