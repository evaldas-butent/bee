package com.butent.bee.client.calendar.monthview;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.DateUtils;

import java.util.Date;

public class WeekLayoutDescription {

  public static final int FIRST_DAY = 0;
  public static final int LAST_DAY = 6;
  private AppointmentStackingManager topAppointmentsManager = null;

  private DayLayoutDescription[] days = null;

  private Date calendarFirstDay = null;
  private Date calendarLastDay = null;

  private int maxLayer = -1;

  public WeekLayoutDescription(Date calendarFirstDay, Date calendarLastDay) {
    this(calendarFirstDay, calendarLastDay, Integer.MAX_VALUE);
  }

  public WeekLayoutDescription(Date calendarFirstDay, Date calendarLastDay, int maxLayer) {
    this.calendarFirstDay = calendarFirstDay;
    this.calendarLastDay = calendarLastDay;
    days = new DayLayoutDescription[7];
    this.maxLayer = maxLayer;
    topAppointmentsManager = new AppointmentStackingManager();
    topAppointmentsManager.setLayerOverflowLimit(this.maxLayer);
  }

  public void addAppointment(Appointment appointment) {
    int dayOfWeek = dayInWeek(appointment.getStart());
    if (appointment.isAllDay()) {
      topAppointmentsManager.assignLayer(new AppointmentLayoutDescription(dayOfWeek, appointment));
    } else {
      initDay(dayOfWeek).addAppointment(appointment);
    }
  }

  public void addMultiDayAppointment(Appointment appointment) {
    int weekStartDay = dayInWeek(appointment.getStart());
    int weekEndDay = dayInWeek(appointment.getEnd());

    if (!appointment.getEnd().before(calendarLastDay)) {
      weekEndDay = LAST_DAY;
    }
    topAppointmentsManager.assignLayer(
        new AppointmentLayoutDescription(weekStartDay, weekEndDay, appointment));
  }

  public void addMultiWeekAppointment(Appointment appointment,
      AppointmentWidgetParts presenceInMonth) {

    switch (presenceInMonth) {
      case FIRST_WEEK:
        int weekStartDay = dayInWeek(appointment.getStart());
        topAppointmentsManager.assignLayer(
            new AppointmentLayoutDescription(weekStartDay, LAST_DAY, appointment));
        break;
      case IN_BETWEEN:
        topAppointmentsManager.assignLayer(
            new AppointmentLayoutDescription(FIRST_DAY, LAST_DAY, appointment));
        break;
      case LAST_WEEK:
        int weekEndDay = dayInWeek(appointment.getEnd());
        topAppointmentsManager.assignLayer(
            new AppointmentLayoutDescription(FIRST_DAY, weekEndDay, appointment));
        break;
    }
  }

  public boolean areThereAppointmentsOnDay(int day) {
    assertValidDayIndex(day);
    return days[day] != null || topAppointmentsManager.areThereAppointmentsOn(day);
  }

  public int currentStackOrderInDay(int dayIndex) {
    return topAppointmentsManager.lowestLayerIndex(dayIndex);
  }

  public DayLayoutDescription getDayLayoutDescription(int day) {
    assertValidDayIndex(day);
    if (!areThereAppointmentsOnDay(day)) {
      return null;
    }
    return days[day];
  }

  public AppointmentStackingManager getTopAppointmentsManager() {
    return topAppointmentsManager;
  }

  private void assertValidDayIndex(int day) {
    if (day < FIRST_DAY || day > days.length) {
      throw new IllegalArgumentException(
          "Invalid day index (" + day + ")");
    }
  }

  private int dayInWeek(Date date) {
    if (date.before(calendarFirstDay)) {
      return FIRST_DAY;
    }
    if (date.after(calendarLastDay)) {
      return LAST_DAY;
    }

    return (int) Math.floor(DateUtils.differenceInDays(date, calendarFirstDay) % 7d);
  }

  private DayLayoutDescription initDay(int day) {
    assertValidDayIndex(day);
    if (days[day] == null) {
      days[day] = new DayLayoutDescription(day);
    }
    return days[day];
  }
}