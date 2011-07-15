package com.butent.bee.client.widget;

import com.google.gwt.i18n.shared.DateTimeFormat;

import com.butent.bee.client.i18n.DateTimeRenderer;
import com.butent.bee.shared.DateTime;

public class DateTimeLabel extends ValueLabel<DateTime> {

  public DateTimeLabel() {
    super(new DateTimeRenderer());
  }

  public DateTimeLabel(DateTimeFormat format) {
    super(new DateTimeRenderer(format));
  }
}
