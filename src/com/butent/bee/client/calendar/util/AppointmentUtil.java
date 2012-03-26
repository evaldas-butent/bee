package com.butent.bee.client.calendar.util;

import com.google.common.collect.Lists;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.DateUtils;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Date;
import java.util.List;

public class AppointmentUtil {

  public static List<Appointment> filterListByDate(List<Appointment> fullList, Date date) {
    List<Appointment> result = Lists.newArrayList();
    
    DateTime start = new DateTime(new JustDate(date));
    DateTime end = new DateTime(TimeUtils.nextDay(new JustDate(date))); 

    for (Appointment appointment : fullList) {
      if (!appointment.isMultiDay() && !appointment.isAllDay()) {
        DateTime from = new DateTime(appointment.getStart());
        DateTime to = new DateTime(appointment.getEnd());
        
        if (BeeUtils.isMeq(from, start) && BeeUtils.isLess(to, end)) {
          result.add(appointment);
        }
      }
    }
    return result;
  }

  public static List<Appointment> filterListByDateRange(List<Appointment> fullList,
      Date date, int days) {
    List<Appointment> result = Lists.newArrayList();
    Date startDate = (Date) date.clone();
    DateUtils.resetTime(startDate);
    Date endDate = DateUtils.shiftDate(date, days);

    for (Appointment appointment : fullList) {
      if ((appointment.isMultiDay() || appointment.isAllDay()) &&
          rangeContains(appointment, startDate, endDate)) {
        result.add(appointment);
      }
    }
    return result;
  }

  @SuppressWarnings("deprecation")
  public static boolean rangeContains(Appointment appt, Date date) {
    Date rangeEnd = (Date) date.clone();
    rangeEnd.setDate(rangeEnd.getDate() + 1);
    DateUtils.resetTime(rangeEnd);
    return rangeContains(appt, date, rangeEnd);
  }

  public static boolean rangeContains(Appointment appointment, Date rangeStart, Date rangeEnd) {
    long apptStartMillis = appointment.getStart().getTime();
    long apptEndMillis = appointment.getEnd().getTime();
    long rangeStartMillis = rangeStart.getTime();
    long rangeEndMillis = rangeEnd.getTime();

    return apptStartMillis >= rangeStartMillis && apptStartMillis < rangeEndMillis
        || apptStartMillis <= rangeStartMillis && apptEndMillis >= rangeStartMillis;
  }
}
