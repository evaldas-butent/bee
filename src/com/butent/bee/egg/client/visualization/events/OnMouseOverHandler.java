package com.butent.bee.egg.client.visualization.events;

import com.butent.bee.egg.client.ajaxloader.Properties;
import com.butent.bee.egg.client.ajaxloader.Properties.TypeException;

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
  protected void onEvent(Properties properties) throws TypeException {
    int row = properties.getNumber("row").intValue();
    int column = properties.getNumber("column").intValue();
    onMouseOverEvent(new OnMouseOverEvent(row, column));
  }
}
