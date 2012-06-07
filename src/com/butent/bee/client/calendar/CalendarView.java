package com.butent.bee.client.calendar;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import java.util.List;

public abstract class CalendarView implements HasSettings {

  public enum Type {
    DAY, MONTH, RESOURCE
  }
  
  private CalendarWidget calendarWidget = null;

  public void attach(CalendarWidget widget) {
    this.calendarWidget = widget;
  }

  public void createAppointment(DateTime start) {
    if (getCalendarWidget() != null) {
      getCalendarWidget().fireTimeBlockClickEvent(start);
    }
  }

  public abstract void doLayout();

  public abstract void doSizing();

  public CalendarSettings getSettings() {
    return getCalendarWidget().getSettings();
  }

  public abstract String getStyleName();
  
  public abstract Type getType();

  public abstract void onAppointmentSelected(Appointment appt);

  public abstract void onDoubleClick(Element element, Event event);

  public abstract void onSingleClick(Element element, Event event);

  public void openAppointment(Appointment appt) {
    if (getCalendarWidget() != null) {
      getCalendarWidget().fireOpenEvent(appt);
    }
  }

  public abstract void scrollToHour(int hour);

  public void selectAppointment(Appointment appt) {
    if (getCalendarWidget() != null) {
      getCalendarWidget().setSelectedAppointment(appt, true);
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
  
  protected int getDays() {
    return getCalendarWidget().getDisplayedDays();
  }
}