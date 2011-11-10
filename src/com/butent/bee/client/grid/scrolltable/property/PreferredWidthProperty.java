package com.butent.bee.client.grid.scrolltable.property;

/**
 * Stores preferred width for a column.
 */

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
