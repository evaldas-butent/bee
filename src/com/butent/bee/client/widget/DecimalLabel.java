package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

import java.math.BigDecimal;

public class DecimalLabel extends NumberLabel<BigDecimal> {

  public DecimalLabel(int scale) {
    super(Format.getDecimalFormat(scale));
  }

  public DecimalLabel(NumberFormat format) {
    super(format);
  }

  public DecimalLabel(String pattern) {
    super(pattern);
  }
}
