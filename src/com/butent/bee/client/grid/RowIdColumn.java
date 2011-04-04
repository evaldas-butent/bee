package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.data.IsRow;

public class RowIdColumn extends Column<IsRow, Number> {
  public RowIdColumn() {
    this(new NumberCell());
  }

  public RowIdColumn(Cell<Number> cell) {
    super(cell);
  }

  @Override
  public Number getValue(IsRow row) {
    if (row == null) {
      return 0L;
    }
    return row.getId();
  }
}
