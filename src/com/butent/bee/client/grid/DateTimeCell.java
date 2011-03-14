package com.butent.bee.client.grid;

import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.shared.SafeHtmlRenderer;

public class DateTimeCell extends DatePickerCell {
  public static DateTimeFormat defaultFormat =
    DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);

  public DateTimeCell() {
    super(defaultFormat);
  }

  public DateTimeCell(DateTimeFormat format, SafeHtmlRenderer<String> renderer) {
    super(format, renderer);
  }

  public DateTimeCell(DateTimeFormat format) {
    super(format);
  }

  public DateTimeCell(SafeHtmlRenderer<String> renderer) {
    super(defaultFormat, renderer);
  }
}
