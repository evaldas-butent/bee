package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

/**
 * Enables using double number type label user interface component.
 */

public class DoubleLabel extends NumberLabel<Double> {

  public DoubleLabel() {
    super(Format.getDefaultDoubleFormat());
  }

  public DoubleLabel(NumberFormat format) {
    super(format);
  }

  public DoubleLabel(String pattern) {
    super(pattern);
  }
}
