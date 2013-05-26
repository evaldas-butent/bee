package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.client.grid.cell.FooterCell;

public class ColumnFooter extends Header<String> {

  private final String columnId;

  public ColumnFooter(String columnId) {
    super(new FooterCell());
    
    this.columnId = columnId;
  }

  @Override
  public String getValue() {
    return columnId;
  }
}
