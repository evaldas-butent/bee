package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

public class LongLabel extends NumberLabel<Long> {

  public LongLabel() {
    super(Format.getDefaultLongFormat());
  }

  public LongLabel(NumberFormat format) {
    super(format);
  }
}
