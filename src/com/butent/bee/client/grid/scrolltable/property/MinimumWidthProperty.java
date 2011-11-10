package com.butent.bee.client.grid.scrolltable.property;

/**
 * Stores minimum width for a column.
 */

public class MinimumWidthProperty implements ColumnProperty {
  public static final String NAME = "min";

  private int minWidth;

  public MinimumWidthProperty(int minWidth) {
    this.minWidth = minWidth;
  }

  public int getMinimumColumnWidth() {
    return minWidth;
  }
}
