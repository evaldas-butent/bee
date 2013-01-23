package com.butent.bee.client.modules.calendar.layout;

import com.google.common.collect.Range;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.layout.WeekLayoutDescription.WidgetPart;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class MonthLayoutDescription {

  private final JustDate firstDate;
  private final Range<DateTime> layoutRange;

  private final WeekLayoutDescription[] weeks;

  public MonthLayoutDescription(JustDate firstDate, int weekCount,
      List<Appointment> appointments, int maxLayer) {
    this.firstDate = firstDate;
    this.layoutRange = Range.closedOpen(firstDate.getDateTime(),
        TimeUtils.nextDay(firstDate, weekCount * 7).getDateTime());

    this.weeks = new WeekLayoutDescription[weekCount];

    placeAppointments(appointments, maxLayer);
  }

  public WeekLayoutDescription[] getWeekDescriptions() {
    return weeks;
  }

  private int calculateWeekFor(DateTime dateTime, boolean end) {
    int diff = TimeUtils.dayDiff(firstDate, dateTime);
    if (end && TimeUtils.minutesSinceDayStarted(dateTime) == 0) {
      diff--;
    }

    if (diff > 0) {
      return Math.min(diff / 7, weeks.length - 1);
    } else {
      return 0;
    }
  }

  private void distributeOverWeeks(int startWeek, int endWeek, Appointment appointment,
      int maxLayer) {

    initWeek(startWeek, maxLayer);
    weeks[startWeek].addMultiWeekAppointment(appointment, WidgetPart.FIRST_WEEK);

    for (int week = startWeek + 1; week < endWeek; week++) {
      initWeek(week, maxLayer);
      weeks[week].addMultiWeekAppointment(appointment, WidgetPart.IN_BETWEEN);
    }

    if (startWeek < endWeek) {
      initWeek(endWeek, maxLayer);
      weeks[endWeek].addMultiWeekAppointment(appointment, WidgetPart.LAST_WEEK);
    }
  }

  private void initWeek(int weekIndex, int maxLayer) {
    if (weeks[weekIndex] == null) {
      JustDate date = TimeUtils.nextDay(firstDate, weekIndex * 7);
      weeks[weekIndex] = new WeekLayoutDescription(date, maxLayer);
    }
  }

  private void placeAppointments(List<Appointment> appointments, int maxLayer) {
    for (Appointment appointment : appointments) {
      if (!BeeUtils.intersects(appointment.getRange(), layoutRange)) {
        continue;
      }

      int startWeek = calculateWeekFor(appointment.getStart(), false);

      if (appointment.isMultiDay()) {
        positionMultiDayAppointment(startWeek, appointment, maxLayer);
      } else {
        initWeek(startWeek, maxLayer);
        weeks[startWeek].addAppointment(appointment);
      }
    }
  }

  private void positionMultiDayAppointment(int startWeek, Appointment appointment, int maxLayer) {
    int endWeek = calculateWeekFor(appointment.getEnd(), true);

    if (startWeek < endWeek) {
      distributeOverWeeks(startWeek, endWeek, appointment, maxLayer);
    } else {
      initWeek(startWeek, maxLayer);
      weeks[startWeek].addMultiDayAppointment(appointment);
    }
  }
}
