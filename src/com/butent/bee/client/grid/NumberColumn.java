package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.shared.data.IsRow;

public class NumberColumn extends CellColumn<Number> {

  public NumberColumn(int index) {
    this(new NumberCell(), index);
  }

  public NumberColumn(NumberFormat format, int index) {
    this(new NumberCell(format), index);
  }
  
  public NumberColumn(Cell<Number> cell, int index) {
    super(cell, index);
  }

  @Override
  public Number getValue(IsRow row) {
    if (row == null) {
      return 0;
    }
    return row.getDouble(getIndex());
  }
}
