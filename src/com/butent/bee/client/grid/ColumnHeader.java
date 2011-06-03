package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.Header;

/**
 * Creates new header cells for grids.
 */

public class ColumnHeader extends Header<String> {

  private final String caption;

  public ColumnHeader(String caption, boolean showWidth) {
    super(new HeaderCell(showWidth));
    this.caption = caption;
  }

  @Override
  public String getValue() {
    return caption;
  }
}
