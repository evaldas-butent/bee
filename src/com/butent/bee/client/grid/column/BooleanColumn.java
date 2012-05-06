package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;

import com.butent.bee.client.grid.cell.BooleanCell;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

/**
 * Implements boolean type column, enables to get value for a specified row or index point.
 */

public class BooleanColumn extends DataColumn<Boolean> {

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
      return null;
    }
    return row.getBoolean(getIndex());
  }
}
