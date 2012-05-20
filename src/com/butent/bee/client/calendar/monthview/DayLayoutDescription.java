package com.butent.bee.client.calendar.monthview;

import com.google.common.collect.Lists;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.Assert;

import java.util.List;

public class DayLayoutDescription {

  private List<Appointment> appointments = Lists.newArrayList();

  private int dayIndex = -1;

  public DayLayoutDescription(int dayIndex) {
    this.dayIndex = dayIndex;
  }

  public void addAppointment(Appointment appointment) {
    Assert.state(!appointment.isMultiDay(),
        "Attempted to add a multiday appointment to a single day description");
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