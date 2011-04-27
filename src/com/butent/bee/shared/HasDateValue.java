package com.butent.bee.shared;

import java.util.Date;

public interface HasDateValue {
  HasDateValue fromDate(JustDate justDate);
  HasDateValue fromDateTime(DateTime dateTime);
  HasDateValue fromJava(Date date);

  JustDate getDate();
  DateTime getDateTime();
  Date getJava();
}
