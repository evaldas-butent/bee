package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.client.grid.cell.HeaderCell;

/**
 * Creates new header cells for grids.
 */

public class ColumnHeader extends Header<String> {
  
  private final String columnId;

  public ColumnHeader(String columnId, String caption) {
    super(new HeaderCell(caption));
    this.columnId = columnId;
  }

  @Override
  public String getValue() {
    return columnId;
  }
}
