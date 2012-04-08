package com.butent.bee.client.datepicker;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.shared.DateTimeFormatInfo;

import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

public class Model {

  public static final int WEEKS_IN_MONTH = 6;
  public static final int DAYS_IN_WEEK = 7;

  public static final int firstDayOfTheWeek;

  public static final int weekendEnd;
  public static final int weekendStart;

  private static final String[] monthNames;
  private static final String[] dayOfWeekNames;
  private static final String[] dayOfMonthNames;

  static {
    DateTimeFormatInfo info = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo();
    firstDayOfTheWeek = info.firstDayOfTheWeek();
    weekendStart = (info.weekendStart() + 5) % 7 + 1;
    weekendEnd = (info.weekendEnd() + 5) % 7 + 1;
    
    monthNames = ArrayUtils.copyOf(info.monthsFull());

    dayOfWeekNames = new String[7]; 
    String[] arr = ArrayUtils.copyOf(info.weekdaysNarrow());
    for (int i = 1; i < arr.length; i++) {
      dayOfWeekNames[i - 1] = arr[i];
    }
    dayOfWeekNames[6] = arr[0];
    
    dayOfMonthNames = new String[31];
    for (int i = 0; i < dayOfMonthNames.length; ++i) {
      dayOfMonthNames[i] = Integer.toString(i + 1);
    }
  }
  
  public static String formatDayOfMonth(JustDate date) {
    return dayOfMonthNames[date.getDom() - 1];
  }

  public static String formatDayOfWeek(int dow) {
    return dayOfWeekNames[dow];
  }

  public static boolean isWeekend(int dow) {
    return dow == weekendStart || dow == weekendEnd;
  }
  
  private final JustDate currentMonth;

  public Model() {
    currentMonth = TimeUtils.startOfMonth();
  }

  public String formatCurrentMonth() {
    return BeeUtils.toString(currentMonth.getYear()) + " " + monthNames[currentMonth.getMonth() - 1];
  }

  public JustDate getCurrentMonth() {
    return currentMonth;
  }

  public boolean isInCurrentMonth(JustDate date) {
    return TimeUtils.sameMonth(currentMonth, date);
  }

  public void setCurrentMonth(JustDate currentDate) {
    this.currentMonth.setDays(TimeUtils.startOfMonth(currentDate, 0).getDays());
  }

  public void shiftCurrentMonth(int deltaMonths) {
    setCurrentMonth(TimeUtils.startOfMonth(currentMonth, deltaMonths));
  }
}
