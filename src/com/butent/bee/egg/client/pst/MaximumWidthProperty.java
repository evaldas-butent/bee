package com.butent.bee.egg.client.pst;

public class MaximumWidthProperty extends ColumnProperty {
  public static final int NO_MAXIMUM_WIDTH = -1;

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

  public MaximumWidthProperty(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public int getMaximumColumnWidth() {
    return maxWidth;
  }
}
