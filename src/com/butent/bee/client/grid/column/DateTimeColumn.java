package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;

import com.butent.bee.client.grid.cell.DateTimeCell;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.time.DateTime;

public class DateTimeColumn extends DataColumn<DateTime> implements HasDateTimeFormat {

  public DateTimeColumn(CellSource cellSource) {
    this(new DateTimeCell(), cellSource);
  }

  public DateTimeColumn(DateTimeFormat format, CellSource cellSource) {
    this(new DateTimeCell(format), cellSource);
  }

  public DateTimeColumn(Cell<DateTime> cell, CellSource cellSource) {
    super(cell, cellSource);
  }

  @Override
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
    return getCellSource().getDateTime(row);
  }

  @Override
  public void setDateTimeFormat(DateTimeFormat format) {
    if (getCell() instanceof HasDateTimeFormat) {
      ((HasDateTimeFormat) getCell()).setDateTimeFormat(format);
    }
  }
}
