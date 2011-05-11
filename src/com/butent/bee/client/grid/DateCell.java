package com.butent.bee.client.grid;

import com.google.gwt.i18n.client.DateTimeFormat;

import com.butent.bee.shared.data.value.ValueType;

/**
 * Extends {@code DatePickerCell} class and is a variation of date picker for date type format.
 */

public class DateCell extends DatePickerCell {
  public static DateTimeFormat defaultFormat = null;

  public DateCell() {
    this(defaultFormat);
  }

  public DateCell(DateTimeFormat format) {
    super(ValueType.DATE, format);
  }
}
