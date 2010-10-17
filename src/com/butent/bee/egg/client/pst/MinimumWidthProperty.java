package com.butent.bee.egg.client.pst;

/**
 * A {@link ColumnProperty} that provides the minimum width of a column.
 */
public class MinimumWidthProperty extends ColumnProperty {
  /**
   * The return value when no minimum width is specified.
   */
  public static final int NO_MINIMUM_WIDTH = -1;

  /**
   * Property type.
   */
  public static final Type<MinimumWidthProperty> TYPE = new Type<MinimumWidthProperty>() {
    private MinimumWidthProperty instance;

    @Override
    public MinimumWidthProperty getDefault() {
      if (instance == null) {
        instance = new MinimumWidthProperty(NO_MINIMUM_WIDTH);
      }
      return instance;
    }
  };

  private int minWidth;

  /**
   * Construct a new {@link MinimumWidthProperty}.
   * 
   * @param minWidth the minimum column width
   */
  public MinimumWidthProperty(int minWidth) {
    this.minWidth = minWidth;
  }

  /**
   * Get the minimum width of the column. A return value of
   * {@link #NO_MINIMUM_WIDTH} indicates that the column has no minimum width,
   * but the consumer of the data may impose one anyway.
   * 
   * @return the minimum allowable width of the column
   */
  public int getMinimumColumnWidth() {
    return minWidth;
  }
}
