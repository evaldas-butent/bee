package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

/**
 * Enables using double number type label user interface component.
 */

public class DoubleLabel extends NumberLabel<Double> {

  public DoubleLabel(boolean inline) {
    super(Format.getDefaultDoubleFormat(), inline);
  }

  public DoubleLabel(NumberFormat format, boolean inline) {
    super(format, inline);
  }

  public DoubleLabel(String pattern, boolean inline) {
    super(pattern, inline);
  }
}
