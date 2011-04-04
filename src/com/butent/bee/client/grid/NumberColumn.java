package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.butent.bee.shared.data.IsRow;

public class NumberColumn extends CellColumn<Number> {

  public NumberColumn(int index, String label) {
    this(new NumberCell(), index, label);
  }

  public NumberColumn(NumberFormat format, int index, String label) {
    this(new NumberCell(format), index, label);
  }
  
  public NumberColumn(Cell<Number> cell, int index, String label) {
    super(cell, index, label);
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LOCALE_END);    
  }

  @Override
  public Number getValue(IsRow row) {
    if (row == null) {
      return 0;
    }
    return row.getDouble(getIndex());
  }
}
