package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;

import com.butent.bee.shared.data.IsRow;

public class BooleanColumn extends CellColumn<Boolean> {

  public BooleanColumn(int index, String label) {
    this(new BooleanCell(), index, label);
  }

  public BooleanColumn(Cell<Boolean> cell, int index, String label) {
    super(cell, index, label);
  }

  @Override
  public Boolean getValue(IsRow row) {
    if (row == null) {
      return false;
    }
    return row.getBoolean(getIndex());
  }
}
