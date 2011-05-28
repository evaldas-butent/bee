package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.i18n.shared.DateTimeFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

/**
 * Implements DateTime type column, enables to get value for a specified row or index point.
 */

public class DateTimeColumn extends CellColumn<DateTime> implements HasDateTimeFormat {

  public DateTimeColumn(int index, IsColumn dataColumn) {
    this(Format.getDefaultDateTimeFormat(), index, dataColumn);
  }

  public DateTimeColumn(DateTimeFormat format, int index, IsColumn dataColumn) {
    this(new DateTimeCell(format), index, dataColumn);
  }

  public DateTimeColumn(Cell<DateTime> cell, int index, IsColumn dataColumn) {
    super(cell, index, dataColumn);
  }

  public DateTimeFormat getDateTimeFormat() {
    if (getCell() instanceof HasDateTimeFormat) {
      return ((HasDateTimeFormat) getCell()).getDateTimeFormat();
    } else {
      return null;
    }
  }
  
  @Override
  public DateTime getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return row.getDateTime(getIndex());
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    if (getCell() instanceof HasDateTimeFormat) {
      ((HasDateTimeFormat) getCell()).setDateTimeFormat(format);
    }
  }
}
