package com.butent.bee.client.i18n;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.JustDate;

public class DateRenderer extends AbstractRenderer<JustDate> {
  private final DateTimeFormat format;

  public DateRenderer() {
    this(Format.getDefaultDateFormat());
  }
  
  public DateRenderer(DateTimeFormat format) {
    this.format = format;
  }

  public String render(JustDate object) {
    if (object == null) {
      return BeeConst.STRING_EMPTY;
    }
    return format.format(object.getJava());
  }
}
