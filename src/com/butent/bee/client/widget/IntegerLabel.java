package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;

public class IntegerLabel extends NumberLabel<Integer> {

  public IntegerLabel() {
    super(Format.getDefaultIntegerFormat());
  }

  public IntegerLabel(NumberFormat format) {
    super(format);
  }
}
