package com.butent.bee.egg.client.pst;

/**
 * A {@link ColumnProperty} that describes whether or not the contents of the
 * column can be sorted.
 */
public class SortableProperty extends ColumnProperty {
  /**
   * Property type.
   */
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

  /**
   * Construct a new {@link SortableProperty}.
   * 
   * @param isSortable true if the column is sortable, false if not
   */
  public SortableProperty(boolean isSortable) {
    this.isSortable = isSortable;
  }

  /**
   * Returns true if the column is sortable, false if it is not.
   * 
   * @return true if the column is sortable, false if it is not sortable
   */
  public boolean isColumnSortable() {
    return isSortable;
  }
}
