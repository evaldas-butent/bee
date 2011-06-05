package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.Header;

/**
 * Creates new header cells for grids.
 */

public class ColumnHeader extends Header<String> {

  private final String columnId;

  public ColumnHeader(String columnId, String caption, boolean showWidth) {
    super(new HeaderCell(caption, showWidth));
    this.columnId = columnId;
  }

  @Override
  public String getValue() {
    return columnId;
  }
}
