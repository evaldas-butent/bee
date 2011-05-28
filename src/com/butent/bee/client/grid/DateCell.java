package com.butent.bee.client.grid;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.JustDate;

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
      sb.appendEscaped((format == null) ? value.toString() : format.format(value.getJava()));
    }
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    this.format = format;
  }
}
