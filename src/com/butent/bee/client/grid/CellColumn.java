package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;

public abstract class CellColumn<C> extends Column<IsRow, C> {
  private int index;

  public CellColumn(Cell<C> cell, int index) {
    super(cell);
    this.index = index;
  }

  protected int getIndex() {
    return index;
  }
  
  protected String getString(IsRow row) {
    if (row == null) {
      return BeeConst.STRING_EMPTY;
    }
    String v = row.getString(index);
    return (v == null) ? BeeConst.STRING_EMPTY : v;
  }
}
