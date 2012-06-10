package com.butent.bee.client.calendar.monthview;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class WeekLayoutDescription {

  public enum WidgetPart {
    FIRST_WEEK, IN_BETWEEN, LAST_WEEK
  }
  
  public static final int FIRST_DAY = 0;
  public static final int LAST_DAY = 6;

  private final AppointmentStackingManager stackingManager;

  private final DayLayoutDescription[] days;

  private final JustDate firstDate;

  public WeekLayoutDescription(JustDate firstDate, int maxLayer) {
    this.firstDate = firstDate;
    
    this.days = new DayLayoutDescription[7];
    
    this.stackingManager = new AppointmentStackingManager(maxLayer);
  }

  public void addAppointment(Appointment appointment) {
    int dayOfWeek = dayInWeek(appointment.getStart(), false);
    initDay(dayOfWeek).addAppointment(appointment);
  }

  public void addMultiDayAppointment(Appointment appointment) {
    int weekStartDay = dayInWeek(appointment.getStart(), false);
    int weekEndDay = dayInWeek(appointment.getEnd(), true);

    stackingManager.assignLayer(weekStartDay, weekEndDay, appointment);
  }

  public void addMultiWeekAppointment(Appointment appointment, WidgetPart part) {
    switch (part) {
      case FIRST_WEEK:
        int weekStartDay = dayInWeek(appointment.getStart(), false);
        stackingManager.assignLayer(weekStartDay, LAST_DAY, appointment);
        break;
      case IN_BETWEEN:
        stackingManager.assignLayer(FIRST_DAY, LAST_DAY, appointment);
        break;
      case LAST_WEEK:
        int weekEndDay = dayInWeek(appointment.getEnd(), true);
        stackingManager.assignLayer(FIRST_DAY, weekEndDay, appointment);
        break;
    }
  }

  public DayLayoutDescription getDayLayoutDescription(int day) {
    return days[day];
  }

  public AppointmentStackingManager getTopAppointmentsManager() {
    return stackingManager;
  }

  private int dayInWeek(DateTime dateTime, boolean end) {
    int diff = TimeUtils.dayDiff(firstDate, dateTime);
    if (end && TimeUtils.minutesSinceDayStarted(dateTime) == 0) {
      diff--;
    }
    return BeeUtils.clamp(diff, FIRST_DAY, LAST_DAY);
  }

  private DayLayoutDescription initDay(int day) {
    if (days[day] == null) {
      days[day] = new DayLayoutDescription(day);
    }
    return days[day];
  }
}