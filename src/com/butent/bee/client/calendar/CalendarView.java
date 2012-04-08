package com.butent.bee.client.calendar;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import com.butent.bee.shared.DateTime;

public abstract class CalendarView implements HasSettings {

  protected CalendarWidget calendarWidget = null;

  private int displayedDays = 4;

  public void attach(CalendarWidget widget) {
    this.calendarWidget = widget;
  }

  public final void createAppointment(Appointment appt) {
    createAppointment(appt.getStart());
  }

  public final void createAppointment(DateTime start) {
    calendarWidget.fireTimeBlockClickEvent(start);
  }

  public final void deleteAppointment(Appointment appt) {
    calendarWidget.fireDeleteEvent(appt);
  }

  public void detatch() {
    calendarWidget = null;
  }

  public abstract void doLayout();

  public void doSizing() {
  }

  public int getDisplayedDays() {
    return displayedDays;
  }

  public CalendarSettings getSettings() {
    return calendarWidget.getSettings();
  }

  public abstract String getStyleName();

  public abstract void onAppointmentSelected(Appointment appt);

  public void onDeleteKeyPressed() {
  }

  public abstract void onDoubleClick(Element element, Event event);

  public void onDownArrowKeyPressed() {
  }

  public void onLeftArrowKeyPressed() {
  }

  public abstract void onMouseOver(Element element, Event event);

  public void onRightArrowKeyPressed() {
  }

  public abstract void onSingleClick(Element element, Event event);

  public void onUpArrowKeyPressed() {
  }

  public final void openAppointment(Appointment appt) {
    calendarWidget.fireOpenEvent(appt);
  }

  public abstract void scrollToHour(int hour);

  public final void selectAppointment(Appointment appt) {
    calendarWidget.setSelectedAppointment(appt, true);
  }

  public final void selectNextAppointment() {
    calendarWidget.selectNextAppointment();
  }

  public final void selectPreviousAppointment() {
    calendarWidget.selectPreviousAppointment();
  }

  public void setDisplayedDays(int displayedDays) {
    this.displayedDays = displayedDays;
  }

  public void setSettings(CalendarSettings settings) {
    calendarWidget.setSettings(settings);
  }

  public final void updateAppointment(Appointment toAppt) {
    calendarWidget.fireUpdateEvent(toAppt);
  }
}