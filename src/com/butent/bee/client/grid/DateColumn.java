package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.butent.bee.shared.HasDateValue;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class DateColumn extends CellColumn<HasDateValue> {

  public DateColumn(int index, IsColumn dataColumn) {
    this(new DateCell(), index, dataColumn);
  }

  public DateColumn(DateTimeFormat format, int index, IsColumn dataColumn) {
    this(new DateCell(format), index, dataColumn);
  }

  public DateColumn(Cell<HasDateValue> cell, int index, IsColumn dataColumn) {
    super(cell, index, dataColumn);
  }

  @Override
  public JustDate getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return row.getDate(getIndex());
  }
}
