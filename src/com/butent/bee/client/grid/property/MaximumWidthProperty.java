package com.butent.bee.client.grid.property;

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
