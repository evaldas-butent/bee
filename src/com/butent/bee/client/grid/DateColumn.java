package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.i18n.shared.DateTimeFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

/**
 * Implements date type column, enables to get value for a specified row or index point.
 */

public class DateColumn extends DataColumn<JustDate> implements HasDateTimeFormat {

  public DateColumn(int index, IsColumn dataColumn) {
    this(Format.getDefaultDateFormat(), index, dataColumn);
  }

  public DateColumn(DateTimeFormat format, int index, IsColumn dataColumn) {
    this(new DateCell(format), index, dataColumn);
  }

  public DateColumn(Cell<JustDate> cell, int index, IsColumn dataColumn) {
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
  public JustDate getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return row.getDate(getIndex());
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    if (getCell() instanceof HasDateTimeFormat) {
      ((HasDateTimeFormat) getCell()).setDateTimeFormat(format);
    }
  }
}
