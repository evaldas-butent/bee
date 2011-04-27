package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class DateTimeColumn extends CellColumn<DateTime> {

  public DateTimeColumn(int index, IsColumn dataColumn) {
    this(new DateTimeCell(), index, dataColumn);
  }

  public DateTimeColumn(DateTimeFormat format, int index, IsColumn dataColumn) {
    this(new DateTimeCell(format), index, dataColumn);
  }

  public DateTimeColumn(Cell<DateTime> cell, int index, IsColumn dataColumn) {
    super(cell, index, dataColumn);
  }

  @Override
  public DateTime getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return row.getDateTime(getIndex());
  }
}
