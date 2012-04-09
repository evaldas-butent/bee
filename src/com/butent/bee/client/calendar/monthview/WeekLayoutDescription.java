package com.butent.bee.client.calendar.monthview;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.shared.HasDateValue;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.TimeUtils;

public class WeekLayoutDescription {

  public static final int FIRST_DAY = 0;
  public static final int LAST_DAY = 6;

  private final AppointmentStackingManager topAppointmentsManager;

  private final DayLayoutDescription[] days;

  private final JustDate calendarFirstDay;

  private final int maxLayer;

  public WeekLayoutDescription(JustDate calendarFirstDay) {
    this(calendarFirstDay, Integer.MAX_VALUE);
  }

  public WeekLayoutDescription(JustDate calendarFirstDay, int maxLayer) {
    this.calendarFirstDay = calendarFirstDay;
    
    this.days = new DayLayoutDescription[7];
    
    this.maxLayer = maxLayer;

    this.topAppointmentsManager = new AppointmentStackingManager();
    this.topAppointmentsManager.setLayerOverflowLimit(this.maxLayer);
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
      throw new IllegalArgumentException("Invalid day index (" + day + ")");
    }
  }

  private int dayInWeek(HasDateValue date) {
    int diff = TimeUtils.dayDiff(calendarFirstDay, date);
    if (diff <= 0) {
      return FIRST_DAY;
    }
    return Math.min(diff % 7, LAST_DAY);
  }

  private DayLayoutDescription initDay(int day) {
    assertValidDayIndex(day);
    if (days[day] == null) {
      days[day] = new DayLayoutDescription(day);
    }
    return days[day];
  }
}