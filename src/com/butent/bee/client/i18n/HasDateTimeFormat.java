package com.butent.bee.client.i18n;

import com.google.gwt.i18n.shared.DateTimeFormat;

/**
 * Requires implementing classes to have methods for getting and setting datetime format.
 */

public interface HasDateTimeFormat {

  DateTimeFormat getDateTimeFormat();

  void setDateTimeFormat(DateTimeFormat format);
}
