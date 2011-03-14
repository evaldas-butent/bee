package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

public abstract class OnMouseOverHandler extends Handler {
  public static class OnMouseOverEvent {
    private int row;
    private int column;

    public OnMouseOverEvent(int row, int column) {
      this.row = row;
      this.column = column;
    }

    public int getColumn() {
      return column;
    }

    public int getRow() {
      return row;
    }
  }

  public abstract void onMouseOverEvent(OnMouseOverEvent event);

  @Override
  protected void onEvent(Properties properties) {
    int row = properties.getInt("row");
    int column = properties.getInt("column");
    onMouseOverEvent(new OnMouseOverEvent(row, column));
  }
}
