package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

public abstract class SelectHandler extends Handler {
  public static class SelectEvent {
  }

  public abstract void onSelect(SelectEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onSelect(new SelectEvent());
  }
}
