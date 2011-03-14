package com.butent.bee.client.grid;

import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.shared.SafeHtmlRenderer;

public class DateCell extends DatePickerCell {
  public static DateTimeFormat defaultFormat =
    DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);

  public DateCell() {
    super(defaultFormat);
  }

  public DateCell(DateTimeFormat format, SafeHtmlRenderer<String> renderer) {
    super(format, renderer);
  }

  public DateCell(DateTimeFormat format) {
    super(format);
  }

  public DateCell(SafeHtmlRenderer<String> renderer) {
    super(defaultFormat, renderer);
  }
}
