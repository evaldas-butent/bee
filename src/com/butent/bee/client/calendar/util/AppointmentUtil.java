package com.butent.bee.client.calendar.util;

import com.google.common.collect.Lists;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.Attendee;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class AppointmentUtil {

  public static List<Appointment> filterListByAttendee(List<Appointment> fullList, long id) {
    List<Appointment> result = Lists.newArrayList();
    for (Appointment appointment : fullList) {
      for (Attendee attendee : appointment.getAttendees()) {
        if (attendee.getId() == id) {
          result.add(appointment);
          break;
        }
      }
    }
    return result;
  }
  
  public static List<Appointment> filterListByDate(List<Appointment> fullList, HasDateValue date) {
    List<Appointment> result = Lists.newArrayList();
    
    DateTime start = TimeUtils.startOfDay(date);
    DateTime end = TimeUtils.startOfDay(date, 1); 

    for (Appointment appointment : fullList) {
      if (!appointment.isMultiDay() && !appointment.isAllDay()) {
        DateTime from = appointment.getStart();
        DateTime to = appointment.getEnd();
        
        if (BeeUtils.isMeq(from, start) && BeeUtils.isLess(to, end)) {
          result.add(appointment);
        }
      }
    }
    return result;
  }

  public static List<Appointment> filterListByDateAndAttendee(List<Appointment> fullList,
      HasDateValue date, long id) {
    List<Appointment> lst = filterListByDate(fullList, date);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterListByAttendee(lst, id);
  }

  public static List<Appointment> filterListByDateRange(List<Appointment> fullList,
      HasDateValue date, int days) {
    List<Appointment> result = Lists.newArrayList();
    DateTime startDate = TimeUtils.startOfDay(date);
    DateTime endDate = TimeUtils.startOfDay(date, days);

    for (Appointment appointment : fullList) {
      if ((appointment.isMultiDay() || appointment.isAllDay()) &&
          rangeContains(appointment, startDate, endDate)) {
        result.add(appointment);
      }
    }
    return result;
  }
  
  public static List<Appointment> filterListByDateRangeAndAttendee(List<Appointment> fullList,
      HasDateValue date, int days, long id) {
    List<Appointment> lst = filterListByDateRange(fullList, date, days);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterListByAttendee(lst, id);
  }
  
  public static boolean rangeContains(Appointment appointment, DateTime rangeStart, DateTime rangeEnd) {
    long apptStartMillis = appointment.getStart().getTime();
    long apptEndMillis = appointment.getEnd().getTime();
    long rangeStartMillis = rangeStart.getTime();
    long rangeEndMillis = rangeEnd.getTime();

    return apptStartMillis >= rangeStartMillis && apptStartMillis < rangeEndMillis
        || apptStartMillis <= rangeStartMillis && apptEndMillis >= rangeStartMillis;
  }
}
