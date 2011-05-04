package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.shared.data.IsColumn;

public class ColumnHeader extends Header<String> {
  private final IsColumn dataColumn;

  public ColumnHeader(IsColumn dataColumn) {
    super(new HeaderCell());
    this.dataColumn = dataColumn;
  }

  @Override
  public String getValue() {
    return dataColumn.getLabel();
  }
}
