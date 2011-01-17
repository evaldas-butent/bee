package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;
import com.butent.bee.client.ajaxloader.Properties.TypeException;

public abstract class SortHandler extends Handler {
  public class SortEvent {
    private int column;
    private boolean ascending;

    public SortEvent(boolean ascending, int column) {
      this.ascending = ascending;
      this.column = column;
    }

    public int getColumn() {
      return column;
    }

    public boolean isAscending() {
     return ascending;
    }
  }

  public abstract void onSort(SortEvent event);

  @Override
  protected void onEvent(Properties event) throws TypeException {
    boolean ascending = event.getBoolean("ascending");
    int column = event.getNumber("column").intValue();
    onSort(new SortEvent(ascending, column));
  }
}