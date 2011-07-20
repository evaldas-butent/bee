package com.butent.bee.client.widget;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.client.i18n.NumberRenderer;

public class NumberLabel<T extends Number> extends ValueLabel<T> implements HasNumberFormat {

  public NumberLabel(NumberFormat format) {
    super(new NumberRenderer(format));
    init();
  }

  public NumberLabel(String pattern) {
    super(new NumberRenderer(pattern));
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
