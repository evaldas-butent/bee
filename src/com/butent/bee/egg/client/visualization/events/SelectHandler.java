package com.butent.bee.egg.client.visualization.events;

import com.butent.bee.egg.client.ajaxloader.Properties;

public abstract class SelectHandler extends Handler {
  public static class SelectEvent {
  }

  public abstract void onSelect(SelectEvent event);

  @Override
  protected void onEvent(Properties properties) {
    onSelect(new SelectEvent());
  }
}
