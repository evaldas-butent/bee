package com.butent.bee.client.modules.calendar.layout;

import com.google.common.collect.Range;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.utils.BeeUtils;

public class AppointmentLayoutDescription {

  private final Appointment appointment;

  private int fromWeekDay = 0;
  private int toWeekDay = 0;

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

  public int getWeekEndDay() {
    return toWeekDay;
  }

  public int getWeekStartDay() {
    return fromWeekDay;
  }

  public boolean overlaps(int from, int to) {
    return BeeUtils.intersects(getRange(), Range.closed(from, to));
  }

  public boolean spansMoreThanADay() {
    return fromWeekDay < toWeekDay;
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
  
  private Range<Integer> getRange() {
    return Range.closed(getWeekStartDay(), getWeekEndDay());
  }
}
