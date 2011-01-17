package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;
import com.butent.bee.client.ajaxloader.Properties.TypeException;

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
  protected void onEvent(Properties properties) throws TypeException {
    int row = properties.getNumber("row").intValue();
    int column = properties.getNumber("column").intValue();
    onMouseOutEvent(new OnMouseOutEvent(row, column));
  }
}
