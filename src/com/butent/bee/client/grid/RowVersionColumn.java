package com.butent.bee.client.grid;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.IsRow;

public class RowVersionColumn extends Column<IsRow, DateTime> {
  
  public static DateTimeFormat defaultFormat = DateTimeFormat.getFormat("yy-MM-dd HH:mm:ss.SSS");

  public RowVersionColumn() {
    this(defaultFormat);
  }

  public RowVersionColumn(DateTimeFormat format) {
    super(new DateTimeCell(format));
  }

  @Override
  public DateTime getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return new DateTime(row.getVersion());
  }
}
