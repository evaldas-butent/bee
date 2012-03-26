package com.butent.bee.client.calendar;

import java.util.Date;

public class DateUtils {

  public static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

  @SuppressWarnings("deprecation")
  public static boolean areOnTheSameDay(Date dateOne, Date dateTwo) {
    return dateOne.getDate() == dateTwo.getDate() &&
        dateOne.getMonth() == dateTwo.getMonth() &&
        dateOne.getYear() == dateTwo.getYear();
  }

  @SuppressWarnings("deprecation")
  public static boolean areOnTheSameMonth(Date dateOne, Date dateTwo) {
    return dateOne.getYear() == dateTwo.getYear() &&
        dateOne.getMonth() == dateTwo.getMonth();
  }

  @SuppressWarnings("deprecation")
  public static void copyTime(Date source, Date target) {
    target.setHours(source.getHours());
    target.setMinutes(source.getMinutes());
    target.setSeconds(source.getSeconds());
  }

  @SuppressWarnings(value = "deprecation")
  public static int differenceInDays(Date endDate, Date startDate) {
    int difference = 0;
    if (!areOnTheSameDay(endDate, startDate)) {
      int endDateOffset = -(endDate.getTimezoneOffset() * 60 * 1000);
      long endDateInstant = endDate.getTime() + endDateOffset;
      int startDateOffset = -(startDate.getTimezoneOffset() * 60 * 1000);
      long startDateInstant = startDate.getTime() + startDateOffset;
      double differenceDouble =
          (double) Math.abs(endDateInstant - startDateInstant) / (double) MILLIS_IN_A_DAY;
      differenceDouble = Math.max(1.0D, differenceDouble);
      difference = (int) differenceDouble;
    }
    return difference;
  }

  @SuppressWarnings("deprecation")
  public static Date firstOfNextMonth(Date date) {
    Date firstOfNextMonth = null;
    if (date != null) {
      int year = (date.getMonth() == 11) ? date.getYear() + 1 : date.getYear();
      firstOfNextMonth = new Date(year, (date.getMonth() + 1) % 12, 1);
    }
    return firstOfNextMonth;
  }

  @SuppressWarnings("deprecation")
  public static Date firstOfPrevMonth(Date date) {
    Date firstOfPrevMonth = null;
    if (date != null) {
      int year = (date.getMonth() == 0) ? date.getYear() - 1 : date.getYear();
      int month = (date.getMonth() == 0) ? 11 : date.getMonth() - 1;
      firstOfPrevMonth = new Date(year, month, 1);
    }
    return firstOfPrevMonth;
  }
  
  @SuppressWarnings("deprecation")
  public static Date firstOfTheMonth(Date anyDayInMonth) {
    Date first = (Date) anyDayInMonth.clone();
    first.setDate(1);
    return first;
  }

  @SuppressWarnings("deprecation")
  public static Date firstOfTheWeek(Date anyDayInWeek) {
    int dow = anyDayInWeek.getDay();
    
    Date first;    
    switch (dow) {
      case 0:
        first = shiftDate(anyDayInWeek, -6);
        break;
      case 1:  
        first = anyDayInWeek;
        break;
      default:
        first = shiftDate(anyDayInWeek, 1 - dow);
    }
    return first;
  }
  
  @SuppressWarnings("deprecation")
  public static boolean isWeekend(final Date day) {
    return day.getDay() == 0 || day.getDay() == 6;
  }

  @SuppressWarnings("deprecation")
  public static int minutesSinceDayStarted(Date day) {
    return day.getHours() * 60 + day.getMinutes();
  }

  @SuppressWarnings("deprecation")
  public static Date moveOneDayForward(Date date) {
    date.setDate(date.getDate() + 1);
    return date;
  }

  public static Date newDate(Date date) {
    Date result = null;
    if (date != null) {
      result = new Date(date.getTime());
    }
    return result;
  }

  public static Date previousDay(Date date) {
    return new Date(date.getTime() - MILLIS_IN_A_DAY);
  }

  @SuppressWarnings("deprecation")
  public static void resetTime(Date date) {
    long milliseconds = safeInMillis(date);
    milliseconds = (milliseconds / 1000) * 1000;
    date.setTime(milliseconds);
    date.setHours(0);
    date.setMinutes(0);
    date.setSeconds(0);
  }

  @SuppressWarnings("deprecation")
  public static Date shiftDate(Date date, int shift) {
    Date result = (Date) date.clone();
    result.setDate(date.getDate() + shift);
    return result;
  }

  @SuppressWarnings("deprecation")
  public static int year(Date date) {
    return 1900 + date.getYear();
  }
  
  private static long safeInMillis(Date date) {
    return date != null ? date.getTime() : 0;
  }
}
