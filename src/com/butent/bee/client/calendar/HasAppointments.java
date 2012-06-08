package com.butent.bee.client.calendar;

import com.butent.bee.client.modules.calendar.Appointment;

import java.util.Collection;

public interface HasAppointments {

  void addAppointment(Appointment appointment, boolean refresh);

  void addAppointments(Collection<Appointment> appointments, boolean refresh);

  void clearAppointments();

  boolean removeAppointment(long id, boolean refresh);
}
