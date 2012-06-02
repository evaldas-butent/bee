package com.butent.bee.client.calendar;

import com.butent.bee.client.modules.calendar.Appointment;

import java.util.Collection;

public interface HasAppointments {

  void addAppointment(Appointment appointment, boolean refresh);

  void addAppointments(Collection<Appointment> appointments, boolean refresh);

  void clearAppointments();

  Appointment getSelectedAppointment();

  boolean hasAppointmentSelected();

  boolean removeAppointment(long id, boolean refresh);
  
  void setSelectedAppointment(Appointment appointment);

  void setSelectedAppointment(Appointment appointment, boolean fireEvents);
}
