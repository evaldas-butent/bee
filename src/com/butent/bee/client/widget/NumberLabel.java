package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.client.i18n.NumberRenderer;

/**
 * Enables using text label user interface component.
 */

public class NumberLabel<T extends Number> extends ValueLabel<T> implements HasNumberFormat {

  public NumberLabel(NumberFormat format, boolean inline) {
    super(new NumberRenderer(format), inline);
    init();
  }

  public NumberLabel(String pattern, boolean inline) {
    super(new NumberRenderer(pattern), inline);
    init();
  }

  public NumberFormat getNumberFormat() {
    return ((NumberRenderer) getRenderer()).getNumberFormat();
  }

  public void setNumberFormat(NumberFormat format) {
    ((NumberRenderer) getRenderer()).setNumberFormat(format);
  }

  private void init() {
    setHorizontalAlignment(ALIGN_LOCALE_END);
  }
}
