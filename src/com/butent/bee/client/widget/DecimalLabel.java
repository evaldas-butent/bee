package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

import java.math.BigDecimal;

/**
 * Enables using decimal type label user interface component.
 */

public class DecimalLabel extends NumberLabel<BigDecimal> {

  public DecimalLabel(int scale, boolean inline) {
    super(Format.getDecimalFormat(scale), inline);
  }

  public DecimalLabel(NumberFormat format, boolean inline) {
    super(format, inline);
  }

  public DecimalLabel(String pattern, boolean inline) {
    super(pattern, inline);
  }
}
