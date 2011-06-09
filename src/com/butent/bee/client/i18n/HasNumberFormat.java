package com.butent.bee.client.i18n;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * Requires implementing classes to have methods for getting and setting number format.
 */

public interface HasNumberFormat {

  NumberFormat getNumberFormat();

  void setNumberFormat(NumberFormat format);
}
