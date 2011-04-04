package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Date;

public class DateColumn extends CellColumn<Date> {

  public DateColumn(int index, String label) {
    this(new DateCell(), index, label);
  }

  public DateColumn(DateTimeFormat format, int index, String label) {
    this(new DateCell(format), index, label);
  }

  public DateColumn(Cell<Date> cell, int index, String label) {
    super(cell, index, label);
  }

  @Override
  public Date getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    JustDate dt = row.getDate(getIndex());
    if (dt == null) {
      return null;
    }
    return TimeUtils.toJava(dt);
  }
}
