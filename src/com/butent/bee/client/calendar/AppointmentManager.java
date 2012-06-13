package com.butent.bee.client.calendar;

import com.google.common.collect.Lists;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.BeeConst;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AppointmentManager {

  private final List<Appointment> appointments = Lists.newArrayList();

  public AppointmentManager() {
    super();
  }

  public void addAppointment(Appointment appt) {
    if (appt != null) {
      appointments.add(appt);
    }
  }

  public void addAppointments(Collection<Appointment> appts) {
    if (appts != null) {
      for (Appointment appointment : appts) {
        addAppointment(appointment);
      }
    }
  }

  public void clearAppointments() {
    appointments.clear();
  }

  public List<Appointment> getAppointments() {
    return appointments;
  }

  public boolean removeAppointment(long id) {
    int index = getAppointmentIndex(id);
    if (BeeConst.isUndef(index)) {
      return false;
    }  

    appointments.remove(index);
    return true;
  }

  public void sortAppointments() {
    Collections.sort(appointments);
  }
  
  private int getAppointmentIndex(long id) {
    for (int i = 0; i < appointments.size(); i++) {
      if (appointments.get(i).getId() == id) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }
}