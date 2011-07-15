package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.client.NumberFormatRenderer;

public class NumberLabel<T extends Number> extends ValueLabel<T> {

  public NumberLabel(NumberFormat format) {
    super(new NumberFormatRenderer(format));
    setHorizontalAlignment(ALIGN_LOCALE_END);
  }
}
