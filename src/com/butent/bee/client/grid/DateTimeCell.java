package com.butent.bee.client.grid;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import com.butent.bee.shared.data.value.ValueType;

/**
 * Extends {@code DatePickerCell} class and is a variation of date picker for DateTime type format.
 */

public class DateTimeCell extends DatePickerCell {
  public static DateTimeFormat defaultFormat =
      DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);

  public DateTimeCell() {
    this(defaultFormat);
  }

  public DateTimeCell(DateTimeFormat format) {
    super(ValueType.DATETIME, format);
  }
}
