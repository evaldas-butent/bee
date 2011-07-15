package com.butent.bee.client.i18n;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;

public class DateTimeRenderer extends AbstractRenderer<DateTime> {
  private final DateTimeFormat format;

  public DateTimeRenderer() {
    this(Format.getDefaultDateTimeFormat());
  }
  
  public DateTimeRenderer(DateTimeFormat format) {
    this.format = format;
  }

  public String render(DateTime object) {
    if (object == null) {
      return BeeConst.STRING_EMPTY;
    }
    return format.format(object.getJava());
  }
}
