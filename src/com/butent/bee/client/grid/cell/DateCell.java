package com.butent.bee.client.grid.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.time.JustDate;

/**
 * Enables using columns which contain date type data.
 */

public class DateCell extends AbstractCell<JustDate> implements HasDateTimeFormat {

  private DateTimeFormat format;

  public DateCell(DateTimeFormat format) {
    super();
    this.format = format;
  }

  public DateTimeFormat getDateTimeFormat() {
    return format;
  }

  @Override
  public void render(Context context, JustDate value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.appendEscaped((format == null) ? value.toString() : format.format(value));
    }
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    this.format = format;
  }
}
