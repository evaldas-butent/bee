package com.butent.bee.client.calendar;

import com.google.gwt.core.client.GWT;

import com.butent.bee.client.calendar.i18n.CalendarConstants;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.TimeUtils;

public class CalendarFormat {

  public static final int HOURS_IN_DAY = 24;

  public static final CalendarConstants MESSAGES;

  public static final CalendarFormat INSTANCE;
  
  private static final DateTimeFormat DEFAULT_DAY_OF_MONTH_FORMAT;
  private static final DateTimeFormat DEFAULT_DAY_OF_WEEK_FORMAT;
  private static final DateTimeFormat DEFAULT_DAY_OF_WEEK_ABBREVIATED_FORMAT;
  private static final DateTimeFormat DEFAULT_HOUR_FORMAT;
  private static final DateTimeFormat DEFAULT_DATE_FORMAT;
  
  private static final String DEFAULT_AM_LABEL;
  private static final String DEFAULT_PM_LABEL;
  private static final String DEFAULT_NOON_LABEL;
  
  static {
    MESSAGES = (CalendarConstants) GWT.create(CalendarConstants.class);
   
    DEFAULT_DAY_OF_MONTH_FORMAT = DateTimeFormat.getFormat(MESSAGES.dayOfMonthFormat());
    DEFAULT_DAY_OF_WEEK_FORMAT = DateTimeFormat.getFormat(MESSAGES.weekdayFormat());
    DEFAULT_DAY_OF_WEEK_ABBREVIATED_FORMAT = DateTimeFormat.getFormat(MESSAGES.weekdayFormat());
    DEFAULT_HOUR_FORMAT = DateTimeFormat.getFormat(MESSAGES.timeFormat());
    DEFAULT_DATE_FORMAT = DateTimeFormat.getFormat(MESSAGES.dateFormat());
    
    DEFAULT_AM_LABEL = MESSAGES.am();
    DEFAULT_PM_LABEL = MESSAGES.pm();
    DEFAULT_NOON_LABEL = MESSAGES.noon();
    
    INSTANCE = new CalendarFormat();
  }

  private String[] weekDayNames = new String[7];
  private String[] dayOfWeekAbbreviatedNames = new String[7];
  private String[] dayOfMonthNames = new String[31];
  private String[] hours = new String[24];

  private DateTimeFormat dayOfMonthFormat = null;
  private DateTimeFormat dayOfWeekFormat = null;
  private DateTimeFormat dayOfWeekAbbreviatedFormat = null;
  
  private DateTimeFormat timeFormat = null;
  private DateTimeFormat dateFormat = null;

  private String am = null;
  private String pm = null;
  private String noon = null;

  private CalendarFormat() {
    this.dayOfMonthFormat = DEFAULT_DAY_OF_MONTH_FORMAT;
    this.dayOfWeekFormat = DEFAULT_DAY_OF_WEEK_FORMAT;
    this.dayOfWeekAbbreviatedFormat = DEFAULT_DAY_OF_WEEK_ABBREVIATED_FORMAT;

    this.timeFormat = DEFAULT_HOUR_FORMAT;
    this.dateFormat = DEFAULT_DATE_FORMAT;
    
    this.am = DEFAULT_AM_LABEL;
    this.pm = DEFAULT_PM_LABEL;
    this.noon = DEFAULT_NOON_LABEL;

    refreshWeekDayNames();
    refreshMonthDayNames();
    generateHourLabels();
  }

  public String getAm() {
    return am;
  }

  public DateTimeFormat getDateFormat() {
    return dateFormat;
  }

  public String[] getDayOfWeekAbbreviatedNames() {
    return dayOfWeekAbbreviatedNames;
  }

  public String[] getDayOfWeekNames() {
    return weekDayNames;
  }

  public String[] getHourLabels() {
    return hours;
  }

  public String getNoon() {
    return noon;
  }

  public String getPm() {
    return pm;
  }

  public DateTimeFormat getTimeFormat() {
    return timeFormat;
  }

  public void setAm(String am) {
    this.am = am;
  }

  public void setDateFormat(String formatPattern) {
    dateFormat = DateTimeFormat.getFormat(formatPattern);
  }
  
  public void setDayOfMonthFormat(String formatPattern) {
    dayOfMonthFormat = DateTimeFormat.getFormat(formatPattern);
    refreshMonthDayNames();
  }

  public void setDayOfWeekAbbreviatedFormat(String formatPattern) {
    dayOfWeekAbbreviatedFormat = DateTimeFormat.getFormat(formatPattern);
    refreshWeekDayNames();
  }

  public void setDayOfWeekFormat(String formatPattern) {
    dayOfWeekFormat = DateTimeFormat.getFormat(formatPattern);
    refreshWeekDayNames();
  }

  public void setHourLabels(String[] hourLabels) {
    if (hourLabels == null || hourLabels.length != HOURS_IN_DAY) {
      return;
    }
    for (int i = 0; i < HOURS_IN_DAY; i++) {
      if (hourLabels[i] != null) {
        hours[i] = hourLabels[i];
      }
    }
  }

  public void setNoon(String noon) {
    this.noon = noon;
  }

  public void setPm(String pm) {
    this.pm = pm;
  }

  public void setTimeFormat(String formatPattern) {
    timeFormat = DateTimeFormat.getFormat(formatPattern);
    generateHourLabels();
  }

  private void generateHourLabels() {
    DateTime date = TimeUtils.startOfDay();
    String hour;

    for (int i = 0; i < HOURS_IN_DAY; i++) {
      date.setHour(i);
      hour = timeFormat.format(date);
      hours[i] = hour;
    }
  }

  private void refreshMonthDayNames() {
    JustDate date = TimeUtils.startOfYear();
    for (int i = 0; i < dayOfMonthNames.length; i++) {
      date.setDom(i + 1);
      dayOfMonthNames[i] = dayOfMonthFormat.format(date);
    }
  }

  private void refreshWeekDayNames() {
    JustDate date = TimeUtils.startOfWeek();
    for (int i = 0; i < 7; i++) {
      weekDayNames[i] = dayOfWeekFormat.format(date);
      dayOfWeekAbbreviatedNames[i] = dayOfWeekAbbreviatedFormat.format(date);
      TimeUtils.moveOneDayForward(date);
    }
  }
}