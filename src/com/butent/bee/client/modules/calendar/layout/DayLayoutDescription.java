package com.butent.bee.client.modules.calendar.layout;

import com.google.common.collect.Lists;

import com.butent.bee.client.modules.calendar.Appointment;

import java.util.List;

public class DayLayoutDescription {

  private final List<Appointment> appointments = Lists.newArrayList();

  private final int dayIndex;

  public DayLayoutDescription(int dayIndex) {
    this.dayIndex = dayIndex;
  }

  public void addAppointment(Appointment appointment) {
    appointments.add(appointment);
  }

  public List<Appointment> getAppointments() {
    return appointments;
  }

  public int getDayIndex() {
    return dayIndex;
  }

  public int getTotalAppointmentCount() {
    return appointments.size();
  }
}