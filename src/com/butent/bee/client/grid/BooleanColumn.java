package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;

import com.butent.bee.shared.data.IsRow;

public class BooleanColumn extends CellColumn<Boolean> {

  public BooleanColumn(int index) {
    this(new BooleanCell(), index);
  }

  public BooleanColumn(Cell<Boolean> cell, int index) {
    super(cell, index);
  }

  @Override
  public Boolean getValue(IsRow row) {
    if (row == null) {
      return false;
    }
    return row.getBoolean(getIndex());
  }
}
