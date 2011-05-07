package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Handles events when an user interface component is minimized or hidden.
 */

public abstract class CollapseHandler extends Handler {
  /**
   * Occurs when an user interface component is minimized or hidden.
   */

  public static class CollapseEvent {
    private boolean collapsed;
    private int row;

    public CollapseEvent(int row, boolean collapsed) {
      this.row = row;
      this.collapsed = collapsed;
    }

    public boolean getCollapsed() {
      return collapsed;
    }

    public int getRow() {
      return row;
    }
  }

  public abstract void onCollapseEvent(CollapseEvent event);

  @Override
  protected void onEvent(Properties properties) {
    int row = properties.getInt("row");
    boolean collapsed = properties.getBoolean("collapsed");
    onCollapseEvent(new CollapseEvent(row, collapsed));
  }
}
