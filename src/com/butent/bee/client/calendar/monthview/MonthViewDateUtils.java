package com.butent.bee.client.calendar.monthview;

import com.butent.bee.client.calendar.DateUtils;
import com.butent.bee.shared.Assert;

import java.util.Date;

public class MonthViewDateUtils {

  @SuppressWarnings("deprecation")
  public static Date firstDateShownInAMonthView(Date dayInMonth,
      int firstDayOfWeek) {
    Date date = DateUtils.firstOfTheMonth(dayInMonth);
    int firstDayOffset = firstDayOfWeek + date.getDate() - date.getDay();
    date.setDate(firstDayOffset);
    if (DateUtils.areOnTheSameMonth(date, dayInMonth) && date.getDate() > 1) {
      date.setDate(firstDayOffset - 7);
    }
    return date;
  }

  @SuppressWarnings("deprecation")
  public static int monthViewRequiredRows(Date dayInMonth, int firstDayOfWeek) {
    int requiredRows = 5;

    Date firstOfTheMonthClone = (Date) dayInMonth.clone();
    firstOfTheMonthClone.setDate(1);

    Date firstDayInCalendar = firstDateShownInAMonthView(dayInMonth, firstDayOfWeek);

    if (firstDayInCalendar.getMonth() != firstOfTheMonthClone.getMonth()) {
      Date lastDayOfPreviousMonth = DateUtils.previousDay(firstOfTheMonthClone);
      int prevMonthOverlap = daysInPeriod(firstDayInCalendar, lastDayOfPreviousMonth);

      Date firstOfNextMonth = DateUtils.firstOfNextMonth(firstOfTheMonthClone);

      int daysInMonth = daysInPeriod(firstOfTheMonthClone,
          DateUtils.previousDay(firstOfNextMonth));

      if (prevMonthOverlap + daysInMonth > 35) {
        requiredRows = 6;
      }
    }
    return requiredRows;
  }

  @SuppressWarnings("deprecation")
  private static int daysInPeriod(Date start, Date end) {
    Assert.isTrue(start.getMonth() == end.getMonth(), "daysInPeriod: " + start.toString() + ", "
        + end.toString() + " dates must be in the same month.");
    return 1 + end.getDate() - start.getDate();
  }
}
