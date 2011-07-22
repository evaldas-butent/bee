package com.butent.bee.client.widget;

import com.google.gwt.i18n.shared.DateTimeFormat;

import com.butent.bee.client.i18n.DateTimeRenderer;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.shared.DateTime;

public class DateTimeLabel extends ValueLabel<DateTime> implements HasDateTimeFormat {

  public DateTimeLabel() {
    super(new DateTimeRenderer());
  }

  public DateTimeLabel(DateTimeFormat format) {
    super(new DateTimeRenderer(format));
  }

  public DateTimeLabel(String pattern) {
    super(new DateTimeRenderer(pattern));
  }

  public DateTimeFormat getDateTimeFormat() {
    return ((DateTimeRenderer) getRenderer()).getDateTimeFormat();
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    ((DateTimeRenderer) getRenderer()).setDateTimeFormat(format);
  }
}
