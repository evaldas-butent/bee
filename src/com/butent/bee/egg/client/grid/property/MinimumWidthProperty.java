package com.butent.bee.egg.client.grid.property;

public class MinimumWidthProperty implements ColumnProperty {
  public static final String NAME = "min";
  
  private int minWidth;

  public MinimumWidthProperty(int minWidth) {
    this.minWidth = minWidth;
  }

  public int getMinimumColumnWidth() {
    return minWidth;
  }
}
