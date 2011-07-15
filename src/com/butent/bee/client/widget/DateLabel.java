package com.butent.bee.client.widget;

import com.google.gwt.i18n.shared.DateTimeFormat;

import com.butent.bee.client.i18n.DateRenderer;
import com.butent.bee.shared.JustDate;

public class DateLabel extends ValueLabel<JustDate> {

  public DateLabel() {
    super(new DateRenderer());
  }

  public DateLabel(DateTimeFormat format) {
    super(new DateRenderer(format));
  }
}
