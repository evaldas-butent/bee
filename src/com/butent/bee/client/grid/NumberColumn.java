package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

/**
 * Implements number type column, enables to get value for a specified row or index point.
 */

public class NumberColumn extends CellColumn<Number> {

  public NumberColumn(int index, IsColumn dataColumn) {
    this(new NumberCell(), index, dataColumn);
  }

  public NumberColumn(NumberFormat format, int index, IsColumn dataColumn) {
    this(new NumberCell(format), index, dataColumn);
  }

  public NumberColumn(Cell<Number> cell, int index, IsColumn dataColumn) {
    super(cell, index, dataColumn);
    setHorizontalAlignment(ALIGN_RIGHT);
  }

  @Override
  public Number getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return row.getDouble(getIndex());
  }
}
