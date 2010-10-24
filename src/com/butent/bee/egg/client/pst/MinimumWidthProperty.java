package com.butent.bee.egg.client.pst;

public class MinimumWidthProperty extends ColumnProperty {
  public static final int NO_MINIMUM_WIDTH = -1;

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

  public MinimumWidthProperty(int minWidth) {
    this.minWidth = minWidth;
  }

  public int getMinimumColumnWidth() {
    return minWidth;
  }
}
