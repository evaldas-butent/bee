package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Date;

public class DateTimeColumn extends CellColumn<Date> {

  public DateTimeColumn(int index, IsColumn dataColumn) {
    this(new DateTimeCell(), index, dataColumn);
  }

  public DateTimeColumn(DateTimeFormat format, int index, IsColumn dataColumn) {
    this(new DateTimeCell(format), index, dataColumn);
  }

  public DateTimeColumn(Cell<Date> cell, int index, IsColumn dataColumn) {
    super(cell, index, dataColumn);
  }

  @Override
  public Date getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    DateTime dt = row.getDateTime(getIndex());
    if (dt == null) {
      return null;
    }
    return TimeUtils.toJava(dt);
  }
}
