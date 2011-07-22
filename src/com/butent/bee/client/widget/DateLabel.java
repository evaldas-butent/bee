package com.butent.bee.client.widget;

import com.google.gwt.i18n.shared.DateTimeFormat;

import com.butent.bee.client.i18n.DateRenderer;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.JustDate;

public class DateLabel extends ValueLabel<JustDate> implements HasDateTimeFormat {

  public DateLabel() {
    super(new DateRenderer());
  }

  public DateLabel(DateTimeFormat format) {
    super(new DateRenderer(format));
  }
  
  public DateLabel(String pattern) {
    super(new DateRenderer(pattern));
  }

  public DateTimeFormat getDateTimeFormat() {
    return ((DateRenderer) getRenderer()).getDateTimeFormat();
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    ((DateRenderer) getRenderer()).setDateTimeFormat(format);
  }
}
