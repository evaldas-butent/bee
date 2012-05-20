package com.butent.bee.client.calendar.monthview;

import com.butent.bee.client.modules.calendar.Appointment;

public class AppointmentLayoutDescription {

  private int stackOrder = 0;

  private int fromWeekDay = 0;

  private int toWeekDay = 0;

  private Appointment appointment = null;

  public AppointmentLayoutDescription(int weekDay, Appointment appointment) {
    this(weekDay, weekDay, appointment);
  }

  public AppointmentLayoutDescription(int fromWeekDay, int toWeekDay, Appointment appointment) {
    this.toWeekDay = toWeekDay;
    this.fromWeekDay = fromWeekDay;
    this.appointment = appointment;
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public int getStackOrder() {
    return stackOrder;
  }

  public int getWeekEndDay() {
    return toWeekDay;
  }

  public int getWeekStartDay() {
    return fromWeekDay;
  }

  public boolean overlapsWithRange(int from, int to) {
    return fromWeekDay >= from && fromWeekDay <= to || fromWeekDay <= from && toWeekDay >= from;
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
  }

  public void setStackOrder(int stackOrder) {
    this.stackOrder = stackOrder;
  }

  public boolean spansMoreThanADay() {
    return fromWeekDay != toWeekDay;
  }

  public AppointmentLayoutDescription split() {
    AppointmentLayoutDescription secondPart = null;
    if (spansMoreThanADay()) {
      secondPart = new AppointmentLayoutDescription(fromWeekDay + 1, toWeekDay, appointment);
      this.toWeekDay = this.fromWeekDay;
    } else {
      secondPart = this;
    }
    return secondPart;
  }

  @Override
  public String toString() {
    return "AppointmentLayoutDescription{" +
        "stackOrder=" + stackOrder +
        ", fromWeekDay=" + fromWeekDay +
        ", toWeekDay=" + toWeekDay +
        ", appointment=[" + appointment.getTitle() + "]@" + appointment.hashCode() + '}';
  }
}
