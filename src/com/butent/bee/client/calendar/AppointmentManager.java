package com.butent.bee.client.calendar;

import com.google.common.collect.Lists;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.BeeConst;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AppointmentManager {

  private Appointment selectedAppointment = null;

  private Appointment rollbackAppointment = null;

  private Appointment committedAppointment = null;

  private final List<Appointment> appointments = Lists.newArrayList();

  private boolean sortPending = false;

  public AppointmentManager() {
    super();
  }

  public void addAppointment(Appointment appt) {
    if (appt != null) {
      appointments.add(appt);
      sortPending = true;
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

  public void commit() {
    rollbackAppointment = null;
    committedAppointment = null;
  }

  public List<Appointment> getAppointments() {
    return appointments;
  }

  public Appointment getRollbackAppointment() {
    return rollbackAppointment;
  }

  public Appointment getSelectedAppointment() {
    return selectedAppointment;
  }

  public boolean hasAppointmentSelected() {
    return selectedAppointment != null;
  }

  public boolean isTheSelectedAppointment(Appointment appointment) {
    return hasAppointmentSelected() && selectedAppointment.equals(appointment);
  }

  public boolean removeAppointment(long id) {
    int index = getAppointmentIndex(id);
    if (BeeConst.isUndef(index)) {
      return false;
    }  

    appointments.remove(index);
    sortPending = true;

    if (hasAppointmentSelected() && getSelectedAppointment().getId() == id) {
      setSelectedAppointment(null);
    }
    return true;
  }

  public void resetSelectedAppointment() {
    if (hasAppointmentSelected()) {
      selectedAppointment = null;
    }
  }

  public void rollback() {
    if (rollbackAppointment == null && committedAppointment == null) {
      return;
    }

    if (committedAppointment == null) {
      addAppointment(rollbackAppointment);
    } else if (rollbackAppointment == null) {
      removeAppointment(committedAppointment.getId());
    } else {
      removeAppointment(committedAppointment.getId());
      addAppointment(rollbackAppointment);
    }

    commit();
  }

  public void setCommittedAppointment(Appointment appt) {
    sortPending = true;
    committedAppointment = appt;
  }

  public void setRollbackAppointment(Appointment appt) {
    sortPending = true;
    commit();
    rollbackAppointment = appt;
  }

  public void setSelectedAppointment(Appointment selectedAppointment) {
    this.selectedAppointment = selectedAppointment;
  }

  public void sortAppointments() {
    if (sortPending) {
      Collections.sort(appointments);
      sortPending = false;
    }
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