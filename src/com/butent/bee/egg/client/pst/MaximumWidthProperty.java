package com.butent.bee.egg.client.pst;

/**
 * A {@link ColumnProperty} that provides the maximum width of a column.
 */
public class MaximumWidthProperty extends ColumnProperty {
  /**
   * The return value when no maximum width is specified.
   */
  public static final int NO_MAXIMUM_WIDTH = -1;

  /**
   * Property type.
   */
  public static final Type<MaximumWidthProperty> TYPE = new Type<MaximumWidthProperty>() {
    private MaximumWidthProperty instance;

    @Override
    public MaximumWidthProperty getDefault() {
      if (instance == null) {
        instance = new MaximumWidthProperty(NO_MAXIMUM_WIDTH);
      }
      return instance;
    }
  };

  private int maxWidth;

  /**
   * Construct a new {@link MaximumWidthProperty}.
   * 
   * @param maxWidth the maximum column width
   */
  public MaximumWidthProperty(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  /**
   * Get the maximum width of the column. A return value of
   * {@link #NO_MAXIMUM_WIDTH} indicates that the column has no maximum width,
   * but the consumer of the data may impose one anyway.
   * 
   * @return the maximum allowable width of the column
   */
  public int getMaximumColumnWidth() {
    return maxWidth;
  }
}
