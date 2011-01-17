package com.butent.bee.client.grid.property;

public class PreferredWidthProperty implements ColumnProperty {
  public static final String NAME = "pref";

  private int preferredWidth;

  public PreferredWidthProperty(int preferredWidth) {
    this.preferredWidth = preferredWidth;
  }
  
  public int getPreferredColumnWidth() {
    return preferredWidth;
  }
}
