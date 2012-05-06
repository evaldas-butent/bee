package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.client.grid.cell.HeaderCell;

/**
 * Creates new header cells for grids.
 */

public class ColumnHeader extends Header<String> {
  
  public static int defaultSortInfoHorizontalSize = 20;
  public static int defaultWidthInfoHorizontalSize = 20;

  private final String columnId;
  private final String caption;
  private final boolean hasWidthInfo;

  public ColumnHeader(String columnId, String caption, boolean showWidth) {
    super(new HeaderCell(caption, showWidth));
    this.columnId = columnId;
    this.caption = caption;
    this.hasWidthInfo = showWidth;
  }

  public String getCaption() {
    return caption;
  }

  @Override
  public String getValue() {
    return columnId;
  }
  
  public boolean hasWidthInfo() {
    return hasWidthInfo;
  }
}
