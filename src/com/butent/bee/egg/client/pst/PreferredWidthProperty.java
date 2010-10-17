package com.butent.bee.egg.client.pst;

/**
 * A {@link ColumnProperty} that provides the preferred width of a column.
 */
public class PreferredWidthProperty extends ColumnProperty {
  /**
   * Property type.
   */
  public static final Type<PreferredWidthProperty> TYPE = new Type<PreferredWidthProperty>() {
    private PreferredWidthProperty instance;

    @Override
    public PreferredWidthProperty getDefault() {
      if (instance == null) {
        instance = new PreferredWidthProperty(80);
      }
      return instance;
    }
  };

  private int preferredWidth;

  /**
   * Construct a new {@link PreferredWidthProperty}.
   * 
   * @param preferredWidth the preferred column width
   */
  public PreferredWidthProperty(int preferredWidth) {
    this.preferredWidth = preferredWidth;
  }
  
  /**
   * Returns the preferred width of the column in pixels. Views should respect
   * the preferred column width and attempt to size the column to its preferred
   * width. If the column must be resized, the preferred width should serve as a
   * weight relative to the preferred widths of other ColumnDefinitions.
   * 
   * @return the preferred width of the column
   */
  public int getPreferredColumnWidth() {
    return preferredWidth;
  }
}
