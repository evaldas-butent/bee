package com.butent.bee.egg.client.grid.property;

public class SortableProperty extends ColumnProperty {
  public static final Type<SortableProperty> TYPE = new Type<SortableProperty>() {
    private SortableProperty instance;

    @Override
    public SortableProperty getDefault() {
      if (instance == null) {
        instance = new SortableProperty(true);
      }
      return instance;
    }
  };

  private boolean isSortable;

  public SortableProperty(boolean isSortable) {
    this.isSortable = isSortable;
  }

  public boolean isColumnSortable() {
    return isSortable;
  }
}
