package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

/**
 * Enables using integer type label user interface component.
 */

public class IntegerLabel extends NumberLabel<Integer> {

  public IntegerLabel() {
    super(Format.getDefaultIntegerFormat());
  }

  public IntegerLabel(NumberFormat format) {
    super(format);
  }

  public IntegerLabel(String pattern) {
    super(pattern);
  }
}
