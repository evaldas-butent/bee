package com.butent.bee.client.grid;

import com.google.gwt.i18n.client.DateTimeFormat;

import com.butent.bee.shared.data.value.ValueType;

public class DateCell extends DatePickerCell {
  public static DateTimeFormat defaultFormat = null;

  public DateCell() {
    this(defaultFormat);
  }

  public DateCell(DateTimeFormat format) {
    super(ValueType.DATE, format);
  }
}
