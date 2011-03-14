package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

public abstract class OnMouseOutHandler extends Handler {
  public static class OnMouseOutEvent {
    private int row;
    private int column;

    public OnMouseOutEvent(int row, int column) {
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

  public abstract void onMouseOutEvent(OnMouseOutEvent event);

  @Override
  protected void onEvent(Properties properties) {
    int row = properties.getInt("row");
    int column = properties.getInt("column");
    onMouseOutEvent(new OnMouseOutEvent(row, column));
  }
}
