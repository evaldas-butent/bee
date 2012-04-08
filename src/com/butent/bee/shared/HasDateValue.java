package com.butent.bee.shared;

import java.util.Date;

/**
 * Requires any implementing classes to be able to get date formats in {@code JustDate},
 * {@code DateTime} and standard Java date type.
 */

public interface HasDateValue extends BeeSerializable {
  
  HasDateValue fromDate(JustDate justDate);

  HasDateValue fromDateTime(DateTime dateTime);

  HasDateValue fromJava(Date date);

  JustDate getDate();

  DateTime getDateTime();
  
  int getDom();
  
  int getDow();
  
  int getDoy();
  
  Date getJava();

  int getMonth();
  
  int getYear();  
}
