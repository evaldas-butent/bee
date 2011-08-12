package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

/**
 * Enables using long number label user interface component.
 */

public class LongLabel extends NumberLabel<Long> {

  public LongLabel() {
    super(Format.getDefaultLongFormat());
  }

  public LongLabel(NumberFormat format) {
    super(format);
  }

  public LongLabel(String pattern) {
    super(pattern);
  }
}
