package com.butent.bee.client.visualization.events;

import com.butent.bee.client.ajaxloader.Properties;

/**
 * Handles sorting events for visualizations.
 */

public abstract class SortHandler extends Handler {

  /**
   * Occurs when a user calls for sorting on a particular column.
   */

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
  protected void onEvent(Properties event) {
    boolean ascending = event.getBoolean("ascending");
    int column = event.getInt("column");
    onSort(new SortEvent(ascending, column));
  }
}