package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

/**
 * Enables using long number label user interface component.
 */

public class LongLabel extends NumberLabel<Long> {

  public LongLabel(boolean inline) {
    super(Format.getDefaultLongFormat(), inline);
  }

  public LongLabel(NumberFormat format, boolean inline) {
    super(format, inline);
  }

  public LongLabel(String pattern, boolean inline) {
    super(pattern, inline);
  }
}
