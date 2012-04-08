package com.butent.bee.client.calendar.monthview;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.shared.HasDateValue;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.List;

public class MonthLayoutDescription {

  private JustDate calendarFirstDay = null;
  private JustDate calendarLastDay = null;

  private WeekLayoutDescription[] weeks = new WeekLayoutDescription[6];

  public MonthLayoutDescription(JustDate calendarFirstDay, int monthViewRequiredRows,
      List<Appointment> appointments) {
    this(calendarFirstDay, monthViewRequiredRows, appointments, Integer.MAX_VALUE);
  }

  public MonthLayoutDescription(JustDate calendarFirstDay, int monthViewRequiredRows,
      List<Appointment> appointments, int maxLayer) {
    this.calendarFirstDay = calendarFirstDay;
    this.calendarLastDay = calculateLastDate(calendarFirstDay, monthViewRequiredRows);
    placeAppointments(appointments, maxLayer);
  }

  public WeekLayoutDescription[] getWeekDescriptions() {
    return weeks;
  }

  private JustDate calculateLastDate(final JustDate startDate, int wks) {
    int daysInMonthGrid = wks * 7;
    JustDate endDate = JustDate.copyOf(startDate);
    TimeUtils.addDay(endDate, daysInMonthGrid - 1);
    return endDate;
  }

  private int calculateWeekFor(HasDateValue testDate, HasDateValue calendarFirstDate) {
    int diff = TimeUtils.dayDiff(calendarFirstDate, testDate);
    if (diff > 0) {
      int week = (int) Math.floor(diff / 7d);
      return Math.min(week, weeks.length - 1);
    } else {
      return 0;
    }
  }

  private void distributeOverWeeks(int startWeek, int endWeek, Appointment appointment, int maxLayer) {
    weeks[startWeek].addMultiWeekAppointment(appointment, AppointmentWidgetParts.FIRST_WEEK);
    for (int week = startWeek + 1; week < endWeek; week++) {
      initWeek(week, maxLayer);
      weeks[week].addMultiWeekAppointment(appointment, AppointmentWidgetParts.IN_BETWEEN);
    }
    if (startWeek < endWeek) {
      initWeek(endWeek, maxLayer);
      weeks[endWeek].addMultiWeekAppointment(appointment, AppointmentWidgetParts.LAST_WEEK);
    }
  }

  private void initWeek(int weekIndex, int maxLayer) {
    if (weeks[weekIndex] == null) {
      weeks[weekIndex] = new WeekLayoutDescription(calendarFirstDay, calendarLastDay, maxLayer);
    }
  }

  private boolean isMultiWeekAppointment(int startWeek, int endWeek) {
    return startWeek != endWeek;
  }

  private boolean overlapsWithMonth(Appointment appointment, JustDate calendarFirstDate,
      JustDate calendarLastDate) {
    return !(TimeUtils.isLess(appointment.getStart(), calendarFirstDate)
        && TimeUtils.isLess(appointment.getEnd(), calendarFirstDate)
        || TimeUtils.isMore(appointment.getStart(), calendarLastDate)
        && TimeUtils.isMore(appointment.getEnd(), calendarLastDate));
  }

  private void placeAppointments(List<Appointment> appointments, int maxLayer) {
    for (Appointment appointment : appointments) {
      if (overlapsWithMonth(appointment, calendarFirstDay, calendarLastDay)) {
        int startWeek = calculateWeekFor(appointment.getStart(), calendarFirstDay);

        if (startWeek >= 0 && startWeek < weeks.length) {
          initWeek(startWeek, maxLayer);
          if (appointment.isMultiDay() || appointment.isAllDay()) {
            positionMultidayAppointment(startWeek, appointment, maxLayer);
          } else {
            weeks[startWeek].addAppointment(appointment);
          }
        }
      }
    }
  }

  private void positionMultidayAppointment(int startWeek, Appointment appointment, int maxLayer) {
    int endWeek = calculateWeekFor(appointment.getEnd(), calendarFirstDay);
    initWeek(endWeek, maxLayer);

    if (isMultiWeekAppointment(startWeek, endWeek)) {
      distributeOverWeeks(startWeek, endWeek, appointment, maxLayer);
    } else {
      weeks[startWeek].addMultiDayAppointment(appointment);
    }
  }
}
