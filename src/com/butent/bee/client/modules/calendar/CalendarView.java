package com.butent.bee.client.modules.calendar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.modules.calendar.event.TimeBlockClickEvent;
import com.butent.bee.client.modules.calendar.event.UpdateEvent;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import java.util.List;

public abstract class CalendarView {

  public enum Type {
    DAY, MONTH, RESOURCE
  }
  
  private CalendarWidget calendarWidget = null;

  public void attach(CalendarWidget widget) {
    this.calendarWidget = widget;
  }

  public void createAppointment(DateTime start, Long attendeeId) {
    if (getCalendarWidget() != null) {
      TimeBlockClickEvent.fire(getCalendarWidget(), start, attendeeId);
    }
  }

  public abstract void doLayout();

  public abstract void doScroll();

  public abstract void doSizing();
  
  public abstract List<AppointmentWidget> getAppointmentWidgets();
  
  public abstract Widget getScrollArea();

  public CalendarSettings getSettings() {
    return getCalendarWidget().getSettings();
  }

  public abstract String getStyleName();
  
  public abstract Type getType();

  public abstract boolean onClick(Element element, Event event);

  public abstract void onClock();
  
  public void openAppointment(Appointment appointment) {
    if (getCalendarWidget() != null) {
      OpenEvent.fire(getCalendarWidget(), appointment);
    }
  }

  public void updateAppointment(Appointment appointment, DateTime newStart, DateTime newEnd,
      int oldColumnIndex, int newColumnIndex, boolean refresh) {
    if (UpdateEvent.fire(getCalendarWidget(), appointment, newStart, newEnd, oldColumnIndex,
        newColumnIndex) || refresh) {
      getCalendarWidget().refresh(false);
    }
  }

  protected void addWidget(Widget widget) {
    getCalendarWidget().addToRootPanel(widget);
  }

  protected List<Appointment> getAppointments() {
    return getCalendarWidget().getAppointments();
  }
  
  protected CalendarWidget getCalendarWidget() {
    return calendarWidget;
  }
  
  protected JustDate getDate() {
    return getCalendarWidget().getDate();
  }

  protected int getDisplayedDays() {
    return getCalendarWidget().getDisplayedDays();
  }
}