package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public abstract class CellColumn<C> extends Column<IsRow, C> {
  private final int index;
  private final IsColumn dataColumn;

  public CellColumn(Cell<C> cell, int index, IsColumn dataColumn) {
    super(cell);
    this.index = index;
    this.dataColumn = dataColumn;
  }

  public IsColumn getDataColumn() {
    return dataColumn;
  }
  
  public int getIndex() {
    return index;
  }

  public String getLabel() {
    return getDataColumn().getLabel();
  }

  protected String getString(IsRow row) {
    if (row == null) {
      return BeeConst.STRING_EMPTY;
    }
    String v = row.getString(index);
    return (v == null) ? BeeConst.STRING_EMPTY : v;
  }
}
