package com.butent.bee.client.grid.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.time.DateTime;

/**
 * Enables using columns which contain time type data.
 */

public class DateTimeCell extends AbstractCell<DateTime> implements HasDateTimeFormat {

  private DateTimeFormat format;

  public DateTimeCell() {
    this(null);
  }
  
  public DateTimeCell(DateTimeFormat format) {
    super();
    this.format = format;
  }

  @Override
  public DateTimeFormat getDateTimeFormat() {
    return format;
  }

  @Override
  public void render(Context context, DateTime value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.appendEscaped((format == null) ? value.toCompactString() : format.format(value));
    }
  }

  @Override
  public void setDateTimeFormat(DateTimeFormat format) {
    this.format = format;
  }
}
