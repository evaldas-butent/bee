package com.butent.bee.egg.client.grid.property;

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
