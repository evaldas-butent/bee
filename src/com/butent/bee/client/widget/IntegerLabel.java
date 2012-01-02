package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

/**
 * Enables using integer type label user interface component.
 */

public class IntegerLabel extends NumberLabel<Integer> {

  public IntegerLabel(boolean inline) {
    super(Format.getDefaultIntegerFormat(), inline);
  }

  public IntegerLabel(NumberFormat format, boolean inline) {
    super(format, inline);
  }

  public IntegerLabel(String pattern, boolean inline) {
    super(pattern, inline);
  }
}
