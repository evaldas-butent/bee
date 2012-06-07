package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class AppointmentUtils {

  public static List<Appointment> filterByAttendee(List<Appointment> input, long id) {
    List<Appointment> result = Lists.newArrayList();
    for (Appointment appointment : input) {
      for (Long attendeeId : appointment.getAttendees()) {
        if (attendeeId == id) {
          result.add(appointment);
          break;
        }
      }
    }
    return result;
  }
  
  public static List<Appointment> filterMulti(List<Appointment> input, JustDate date, int days) {
    List<Appointment> result = Lists.newArrayList();
    DateTime min = TimeUtils.startOfDay(date);
    DateTime max = TimeUtils.startOfDay(date, days);

    for (Appointment appointment : input) {
      if (appointment.isMultiDay() && rangeContains(appointment, min, max)) {
        result.add(appointment);
      }
    }
    return result;
  }

  public static List<Appointment> filterMulti(List<Appointment> input, JustDate date, int days,
      long id) {
    List<Appointment> lst = filterMulti(input, date, days);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterByAttendee(lst, id);
  }

  public static List<Appointment> filterSimple(List<Appointment> input, JustDate date) {
    List<Appointment> result = Lists.newArrayList();
    
    DateTime min = TimeUtils.startOfDay(date);
    DateTime max = TimeUtils.startOfDay(date, 1); 

    for (Appointment appointment : input) {
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
  
  public static List<Appointment> filterSimple(List<Appointment> input, JustDate date, long id) {
    List<Appointment> lst = filterSimple(input, date);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterByAttendee(lst, id);
  }
  
  public static Range<DateTime> getRange(Appointment appointment) {
    return Ranges.closedOpen(appointment.getStart(), appointment.getEnd());
  }
  
  public static boolean rangeContains(Appointment appointment, DateTime min, DateTime max) {
    DateTime start = appointment.getStart();
    DateTime end = appointment.getEnd();
    
    return TimeUtils.isLess(start, max) && TimeUtils.isMore(end, min);
  }
}
