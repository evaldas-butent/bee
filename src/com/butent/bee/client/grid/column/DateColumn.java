package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell;

import com.butent.bee.client.grid.cell.DateCell;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.time.JustDate;

public class DateColumn extends DataColumn<JustDate> implements HasDateTimeFormat {

  public DateColumn(CellSource cellSource) {
    this(Format.getDefaultDateFormat(), cellSource);
  }

  public DateColumn(DateTimeFormat format, CellSource cellSource) {
    this(new DateCell(format), cellSource);
  }

  public DateColumn(Cell<JustDate> cell, CellSource cellSource) {
    super(cell, cellSource);
  }

  public DateTimeFormat getDateTimeFormat() {
    if (getCell() instanceof HasDateTimeFormat) {
      return ((HasDateTimeFormat) getCell()).getDateTimeFormat();
    } else {
      return null;
    }
  }

  @Override
  public JustDate getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return getCellSource().getDate(row);
  }

  @Override
  public void setDateTimeFormat(DateTimeFormat format) {
    if (getCell() instanceof HasDateTimeFormat) {
      ((HasDateTimeFormat) getCell()).setDateTimeFormat(format);
    }
  }
}
