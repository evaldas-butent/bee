package com.butent.bee.client.i18n;

import com.google.gwt.i18n.client.NumberFormat;

public interface HasNumberFormat {
  
  NumberFormat getNumberFormat();
  
  void setNumberFormat(NumberFormat format);
}
