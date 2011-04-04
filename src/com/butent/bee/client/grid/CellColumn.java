package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;

public abstract class CellColumn<C> extends Column<IsRow, C> {
  private int index;
  private String label;

  public CellColumn(Cell<C> cell, int index, String label) {
    super(cell);
    this.index = index;
    this.label = label;
  }

  public int getIndex() {
    return index;
  }
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  protected String getString(IsRow row) {
    if (row == null) {
      return BeeConst.STRING_EMPTY;
    }
    String v = row.getString(index);
    return (v == null) ? BeeConst.STRING_EMPTY : v;
  }
}
