package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class BooleanColumn extends CellColumn<Boolean> {

  public BooleanColumn(int index, IsColumn dataColumn) {
    this(new BooleanCell(), index, dataColumn);
  }

  public BooleanColumn(Cell<Boolean> cell, int index, IsColumn dataColumn) {
    super(cell, index, dataColumn);
    setHorizontalAlignment(ALIGN_CENTER);
  }

  @Override
  public Boolean getValue(IsRow row) {
    if (row == null) {
      return false;
    }
    return row.getBoolean(getIndex());
  }
}
