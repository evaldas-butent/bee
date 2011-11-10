package com.butent.bee.client.grid.scrolltable.property;

/**
 * Stores maximum width for a column.
 */

public class MaximumWidthProperty implements ColumnProperty {
  public static final String NAME = "max";

  private int maxWidth;

  public MaximumWidthProperty(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public int getMaximumColumnWidth() {
    return maxWidth;
  }
}
