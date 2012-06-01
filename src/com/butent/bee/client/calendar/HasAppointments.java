package com.butent.bee.client.calendar;

import com.butent.bee.client.modules.calendar.Appointment;

import java.util.Collection;

public interface HasAppointments {

  void addAppointment(Appointment appointment);

  void addAppointments(Collection<Appointment> appointments);

  void clearAppointments();

  Appointment getSelectedAppointment();

  boolean hasAppointmentSelected();

  void setSelectedAppointment(Appointment appointment);

  void setSelectedAppointment(Appointment appointment, boolean fireEvents);
}
