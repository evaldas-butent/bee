package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class CalendarUtils {

  private static final String PERIOD_SEPARATOR = " - ";

  private static final DateTimeFormat DATE_TIME_FORMAT =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);
  private static final DateTimeFormat DATE_FORMAT =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
  private static final DateTimeFormat TIME_FORMAT =
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_SHORT);

  private static final DateTimeFormat MONTH_DAY = DateTimeFormat.getFormat("MM-dd");
  private static final DateTimeFormat MONTH_DAY_TIME = DateTimeFormat.getFormat("MM-dd HH:mm");
  
  public static List<Appointment> filterByAttendee(Collection<Appointment> input, long id) {
    List<Appointment> result = Lists.newArrayList();
    for (Appointment appointment : input) {
      if (appointment.getAttendees().contains(id)) {
        result.add(appointment);
      }
    }
    return result;
  }

  public static List<Appointment> filterByAttendees(Collection<Appointment> input,
      Collection<Long> attIds, boolean separate) {
    List<Appointment> result = Lists.newArrayList();

    for (Appointment appointment : input) {
      for (Long id : appointment.getAttendees()) {
        if (attIds.contains(id)) {
    
          if (separate) {
            Appointment copy = new Appointment(appointment.getRow(), id);
            result.add(copy);
          } else {
            result.add(appointment);
            break;
          }
        }
      }
    }
    return result;
  }

  public static List<Appointment> filterByRange(Collection<Appointment> input, JustDate date,
      int days) {
    List<Appointment> result = Lists.newArrayList();
    long min = TimeUtils.startOfDay(date).getTime();
    long max = TimeUtils.startOfDay(date, days).getTime();

    for (Appointment appointment : input) {
      if (intersects(appointment, min, max)) {
        result.add(appointment);
      }
    }
    return result;
  }
  
  public static List<Appointment> filterMulti(Collection<Appointment> input, JustDate date,
      int days) {
    List<Appointment> result = Lists.newArrayList();
    long min = TimeUtils.startOfDay(date).getTime();
    long max = TimeUtils.startOfDay(date, days).getTime();

    for (Appointment appointment : input) {
      if (intersects(appointment, min, max) && appointment.isMultiDay()) {
        result.add(appointment);
      }
    }
    return result;
  }

  public static List<Appointment> filterMulti(Collection<Appointment> input, JustDate date,
      int days, Collection<Long> attIds, boolean separate) {
    List<Appointment> lst = filterMulti(input, date, days);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterByAttendees(lst, attIds, separate);
  }

  public static List<Appointment> filterMulti(Collection<Appointment> input, JustDate date,
      int days, long id) {
    List<Appointment> lst = filterMulti(input, date, days);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterByAttendee(lst, id);
  }
  
  public static List<Appointment> filterSimple(Collection<Appointment> input, JustDate date) {
    List<Appointment> result = Lists.newArrayList();

    long min = TimeUtils.startOfDay(date).getTime();
    long max = TimeUtils.startOfDay(date, 1).getTime();

    for (Appointment appointment : input) {
      if (appointment.getStartMillis() >= min && appointment.getEndMillis() <= max) {
        result.add(appointment);
      }
    }
    return result;
  }

  public static List<Appointment> filterSimple(Collection<Appointment> input, JustDate date,
      Collection<Long> attIds, boolean separate) {
    List<Appointment> lst = filterSimple(input, date);
    if (lst.isEmpty()) {
      return lst;
    }
    return filterByAttendees(lst, attIds, separate);
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

  public static AppointmentWidget getAppointmentWidget(Widget child) {
    for (Widget widget = child; widget != null; widget = widget.getParent()) {
      if (widget instanceof AppointmentWidget) {
        return (AppointmentWidget) widget;
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

  public static int getEndHour(Collection<AppointmentWidget> widgets) {
    int result = BeeConst.UNDEF;
    if (widgets == null) {
      return result;
    }

    for (AppointmentWidget widget : widgets) {
      if (!widget.isMulti()) {
        DateTime end = widget.getAppointment().getEnd();
        int hour = end.getHour();
        if (end.getMinute() > 0) {
          hour++;
        } else if (hour == 0) {
          hour = TimeUtils.HOURS_PER_DAY;
        }

        if (BeeConst.isUndef(result)) {
          result = hour;
        } else {
          result = Math.max(result, hour);
        }
      }
    }
    return result;
  }

  public static int getEndPixels(CalendarSettings settings,
      Collection<AppointmentWidget> widgets) {
    Assert.notNull(settings);

    int hour = settings.getWorkingHourEnd();
    int maxHour = getEndHour(widgets);

    if (hour > 0 || maxHour > 0) {
      hour = Math.max(hour, maxHour);
    } else {
      hour = TimeUtils.HOURS_PER_DAY;
    }

    return hour * settings.getHourHeight();
  }

  public static int getIntervalStartPixels(DateTime dt, CalendarSettings settings) {
    Assert.notNull(dt);
    Assert.notNull(settings);
    
    int mpi = TimeUtils.MINUTES_PER_HOUR / settings.getIntervalsPerHour();
    
    return dt.getHour() * settings.getHourHeight() 
        + dt.getMinute() / mpi * settings.getPixelsPerInterval();
  }

  public static int getMinutes(int y, CalendarSettings settings) {
    int hour = y / settings.getHourHeight();

    int interval = (y - hour * settings.getHourHeight()) / settings.getPixelsPerInterval();
    int minute = interval * TimeUtils.MINUTES_PER_HOUR / settings.getIntervalsPerHour();

    return hour * TimeUtils.MINUTES_PER_HOUR + minute;
  }

  public static int getNowY(CalendarSettings settings) {
    DateTime now = new DateTime();
    int hourHeight = settings.getHourHeight();

    return now.getHour() * hourHeight + now.getMinute() * hourHeight / TimeUtils.MINUTES_PER_HOUR;
  }
  
  public static int getStartHour(Collection<AppointmentWidget> widgets) {
    int result = BeeConst.UNDEF;
    if (widgets == null) {
      return result;
    }

    for (AppointmentWidget widget : widgets) {
      if (!widget.isMulti()) {
        int hour = widget.getAppointment().getStart().getHour();
        if (BeeConst.isUndef(result)) {
          result = hour;
        } else {
          result = Math.min(result, hour);
        }
      }
    }
    return result;
  }

  public static int getStartPixels(CalendarSettings settings,
      Collection<AppointmentWidget> widgets) {
    Assert.notNull(settings);

    int hour = settings.getScrollToHour();
    if (hour <= 0) {
      hour = Math.max(settings.getWorkingHourStart(), 0);
    }

    int minHour = getStartHour(widgets);
    if (minHour >= 0) {
      hour = Math.min(hour, minHour);
    }

    return hour * settings.getHourHeight();
  }

  public static int getTodayColumn(JustDate date, int days) {
    int diff = TimeUtils.dayDiff(date, TimeUtils.today());
    return BeeUtils.betweenExclusive(diff, 0, days) ? diff : BeeConst.UNDEF;
  }

  public static String renderDateTime(DateTime dateTime) {
    if (dateTime.getYear() == TimeUtils.today().getYear()) {
      if (dateTime.getHour() > 0 || dateTime.getMinute() > 0) {
        return MONTH_DAY_TIME.format(dateTime);
      } else {
        return MONTH_DAY.format(dateTime);
      }

    } else {
      if (dateTime.getHour() > 0 || dateTime.getMinute() > 0) {
        return DATE_TIME_FORMAT.format(dateTime);
      } else {
        return DATE_FORMAT.format(dateTime);
      }
    }
  }
  
  public static String renderPeriod(DateTime start, DateTime end) {
    if (start == null) {
      if (end == null) {
        return BeeConst.STRING_EMPTY;
      } else {
        return PERIOD_SEPARATOR + renderDateTime(end);
      }

    } else if (end == null) {
      return renderDateTime(start) + PERIOD_SEPARATOR;

    } else if (TimeUtils.sameDate(start, end)) {
      return renderDateTime(start) + PERIOD_SEPARATOR + TIME_FORMAT.format(end);

    } else {
      return renderDateTime(start) + PERIOD_SEPARATOR + renderDateTime(end);
    }
  }

  public static String renderRange(Range<DateTime> range) {
    return (range == null) ? BeeConst.STRING_EMPTY 
        : renderPeriod(range.lowerEndpoint(), range.upperEndpoint()); 
  }
  
  private static boolean intersects(Appointment appointment, long min, long max) {
    return appointment.getStartMillis() < max && appointment.getEndMillis() > min;
  }
}
