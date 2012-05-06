package com.butent.bee.client.grid.column;

import com.google.gwt.cell.client.Cell.Context;

import com.butent.bee.client.grid.cell.DateTimeCell;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Manages a column with a timestamp for the last edit time of particular row.
 */

public class RowVersionColumn extends AbstractColumn<DateTime> implements HasDateTimeFormat {

  public static DateTimeFormat defaultFormat = DateTimeFormat.getFormat("yy-MM-dd HH:mm:ss.SSS");

  public RowVersionColumn() {
    this(defaultFormat);
  }

  public RowVersionColumn(DateTimeFormat format) {
    super(new DateTimeCell(format));
  }

  @Override
  public ColType getColType() {
    return ColType.VERSION;
  }

  public DateTimeFormat getDateTimeFormat() {
    if (getCell() instanceof HasDateTimeFormat) {
      return ((HasDateTimeFormat) getCell()).getDateTimeFormat();
    }
    return null;
  }

  @Override
  public String getString(Context context, IsRow row) {
    if (row == null) {
      return null;
    }
    return BeeUtils.toString(row.getVersion());
  }

  @Override
  public DateTime getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return new DateTime(row.getVersion());
  }

  @Override
  public ValueType getValueType() {
    return ValueType.DATETIME;
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    if (getCell() instanceof HasDateTimeFormat) {
      ((HasDateTimeFormat) getCell()).setDateTimeFormat(format);
    }
  }
}
