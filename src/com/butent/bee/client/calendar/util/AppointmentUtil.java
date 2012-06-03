package com.butent.bee.client.calendar.util;

import com.google.common.collect.Lists;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class AppointmentUtil {

  public static List<Appointment> filterListByAttendee(List<Appointment> fullList, long id) {
    List<Appointment> result = Lists.newArrayList();
    for (Appointment appointment : fullList) {
      for (Long attendeeId : appointment.getAttendees()) {
        if (attendeeId == id) {
          result.add(appointment);
          break;
        }
      }
    }
    return result;
  }
  
  public static List<Appointment> filterListByDate(List<Appointment> fullList, JustDate date) {
    List<Appointment> result = Lists.newArrayList();
    
    DateTime min = TimeUtils.startOfDay(date);
    DateTime max = TimeUtils.startOfDay(date, 1); 

    for (Appointment appointment : fullList) {
      if (!appointment.isMultiDay()) {
        DateTime start = appointment.getStart();
        DateTime end = appointment.getEnd();
        
        if (BeeUtils.isMeq(start, min) && BeeUtils.isLeq(end, max)) {
          result.add(appointment);
        }
      }
    }
    return result;
  }

  public static List<Appointment> filterListByDateAndAttendee(List<Appointment> fullList,
      JustDate date, long id) {
    List<Appointment> lst = filterListByDate(fullList, date);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterListByAttendee(lst, id);
  }

  public static List<Appointment> filterListByDateRange(List<Appointment> fullList,
      JustDate date, int days) {
    List<Appointment> result = Lists.newArrayList();
    DateTime min = TimeUtils.startOfDay(date);
    DateTime max = TimeUtils.startOfDay(date, days);

    for (Appointment appointment : fullList) {
      if (appointment.isMultiDay() && rangeContains(appointment, min, max)) {
        result.add(appointment);
      }
    }
    return result;
  }
  
  public static List<Appointment> filterListByDateRangeAndAttendee(List<Appointment> fullList,
      JustDate date, int days, long id) {
    List<Appointment> lst = filterListByDateRange(fullList, date, days);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterListByAttendee(lst, id);
  }
  
  public static boolean rangeContains(Appointment appointment, DateTime min, DateTime max) {
    DateTime start = appointment.getStart();
    DateTime end = appointment.getEnd();
    
    return TimeUtils.isLess(start, max) && TimeUtils.isMore(end, min);
  }
}
