package com.butent.bee.client.calendar;

import com.butent.bee.client.modules.calendar.Appointment;

import java.util.Collection;

public interface HasAppointments {

  void addAppointment(Appointment appointment);

  void addAppointments(Collection<Appointment> appointments);

  void clearAppointments();

  Appointment getSelectedAppointment();

  boolean hasAppointmentSelected();

  void removeAppointment(Appointment appointment);

  void removeAppointment(Appointment appointment, boolean fireEvents);

  void setSelectedAppointment(Appointment appointment);

  void setSelectedAppointment(Appointment appointment, boolean fireEvents);
}
