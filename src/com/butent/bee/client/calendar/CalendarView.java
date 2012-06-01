package com.butent.bee.client.calendar;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;

public abstract class CalendarView implements HasSettings {

  public enum Type {
    DAY, MONTH, RESOURCE
  }
  
  protected CalendarWidget calendarWidget = null;

  private int displayedDays = BeeConst.UNDEF;

  public void attach(CalendarWidget widget) {
    Assert.notNull(widget);
    this.calendarWidget = widget;
  }

  public void createAppointment(DateTime start) {
    Assert.notNull(calendarWidget);
    calendarWidget.fireTimeBlockClickEvent(start);
  }

  public abstract void doLayout();

  public abstract void doSizing();

  public int getDisplayedDays() {
    return displayedDays;
  }

  public CalendarSettings getSettings() {
    Assert.notNull(calendarWidget);
    return calendarWidget.getSettings();
  }

  public abstract String getStyleName();
  
  public abstract Type getType();

  public abstract void onAppointmentSelected(Appointment appt);

  public abstract void onDoubleClick(Element element, Event event);

  public abstract void onMouseOver(Element element, Event event);

  public abstract void onSingleClick(Element element, Event event);

  public void openAppointment(Appointment appt) {
    Assert.notNull(calendarWidget);
    calendarWidget.fireOpenEvent(appt);
  }

  public abstract void scrollToHour(int hour);

  public void selectAppointment(Appointment appt) {
    Assert.notNull(calendarWidget);
    calendarWidget.setSelectedAppointment(appt, true);
  }

  public void setDisplayedDays(int displayedDays) {
    this.displayedDays = displayedDays;
  }
}