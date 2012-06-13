package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dnd.DragContext;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class CalendarUtils {

  public static List<Appointment> filterByAttendee(Collection<Appointment> input, long id) {
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

  public static List<Appointment> filterMulti(Collection<Appointment> input, JustDate date,
      int days) {
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

  public static List<Appointment> filterMulti(Collection<Appointment> input, JustDate date, int days,
      long id) {
    List<Appointment> lst = filterMulti(input, date, days);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterByAttendee(lst, id);
  }

  public static List<Appointment> filterSimple(Collection<Appointment> input, JustDate date) {
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

  public static List<Appointment> filterSimple(Collection<Appointment> input, JustDate date,
      long id) {
    List<Appointment> lst = filterSimple(input, date);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterByAttendee(lst, id);
  }

  public static AppointmentWidget findWidget(Collection<AppointmentWidget> widgets,
      Element element) {
    if (widgets.isEmpty() || element == null) {
      return null;
    }

    for (AppointmentWidget widget : widgets) {
      if (widget.getElement().isOrHasChild(element)) {
        return widget;
      }
    }
    return null;
  }

  public static int getColumnWidth(Widget widget, int columnCount) {
    int totalWidth = widget.getElement().getClientWidth();
    if (columnCount <= 1) {
      return totalWidth;
    } else {
      return totalWidth * (100 / columnCount) / 100;
    }
  }
  
  public static int getCoordinateMinutesSinceDayStarted(int y, CalendarSettings settings) {
    int hour = y / settings.getHourHeight();
    
    int interval = (y - hour * settings.getHourHeight()) / settings.getPixelsPerInterval();
    int minute = interval * TimeUtils.MINUTES_PER_HOUR / settings.getIntervalsPerHour();
    
    return hour * TimeUtils.MINUTES_PER_HOUR + minute;
  }
  
  public static Appointment getDragAppointment(DragContext context) {
    AppointmentWidget appointmentWidget = getDragAppointmentWidget(context);
    return (appointmentWidget == null) ? null : appointmentWidget.getAppointment();
  }

  public static AppointmentWidget getDragAppointmentWidget(DragContext context) {
    Widget widget = context.draggable;
    
    while (widget != null) {
      if (widget instanceof AppointmentWidget) {
        return (AppointmentWidget) widget;
      }
      widget = widget.getParent();
    }
    return null;
  }
  
  public static int getNowY(CalendarSettings settings) {
    DateTime now = new DateTime();
    int hourHeight = settings.getHourHeight();
    
    return now.getHour() * hourHeight + now.getMinute() * hourHeight / TimeUtils.MINUTES_PER_HOUR;
  }
  
  public static Range<DateTime> getRange(Appointment appointment) {
    return Ranges.closedOpen(appointment.getStart(), appointment.getEnd());
  }
  
  public static int getTodayColumn(JustDate date, int days) {
    int diff = TimeUtils.dayDiff(date, TimeUtils.today());
    return BeeUtils.betweenExclusive(diff, 0, days) ? diff : BeeConst.UNDEF;
  }

  public static boolean rangeContains(Appointment appointment, DateTime min, DateTime max) {
    DateTime start = appointment.getStart();
    DateTime end = appointment.getEnd();

    return TimeUtils.isLess(start, max) && TimeUtils.isMore(end, min);
  }
}
