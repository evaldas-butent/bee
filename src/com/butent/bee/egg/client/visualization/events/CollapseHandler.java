package com.butent.bee.egg.client.visualization.events;

import com.butent.bee.egg.client.ajaxloader.Properties;
import com.butent.bee.egg.client.ajaxloader.Properties.TypeException;

public abstract class CollapseHandler extends Handler {
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
  protected void onEvent(Properties properties) throws TypeException {
    int row = properties.getNumber("row").intValue();
    boolean collapsed = properties.getBoolean("collapsed");
    onCollapseEvent(new CollapseEvent(row, collapsed));
  }
}
