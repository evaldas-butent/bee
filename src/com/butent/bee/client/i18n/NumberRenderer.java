package com.butent.bee.client.i18n;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

public class NumberRenderer extends AbstractRenderer<Number> implements HasNumberFormat {

  private NumberFormat format;

  public NumberRenderer() {
    this.format = null;
  }

  public NumberRenderer(NumberFormat format) {
    this.format = format;
  }

  public NumberRenderer(String pattern) {
    this.format = BeeUtils.isEmpty(pattern) ? null : Format.getNumberFormat(pattern);
  }
  
  public NumberFormat getNumberFormat() {
    return format;
  }

  public String render(Number object) {
    if (object == null) {
      return BeeConst.STRING_EMPTY;
    } else if (getNumberFormat() == null) {
      return object.toString();
    } else {
      return getNumberFormat().format(object);
    }
  }

  public void setNumberFormat(NumberFormat format) {
    this.format = format;
  }
}
