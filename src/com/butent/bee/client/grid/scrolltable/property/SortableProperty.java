package com.butent.bee.client.grid.scrolltable.property;

/**
 * Stores information whether a certain column is sortable or not.
 */

public class SortableProperty implements ColumnProperty {
  public static final String NAME = "sort";

  private boolean isSortable;

  public SortableProperty(boolean isSortable) {
    this.isSortable = isSortable;
  }

  public boolean isColumnSortable() {
    return isSortable;
  }
}
