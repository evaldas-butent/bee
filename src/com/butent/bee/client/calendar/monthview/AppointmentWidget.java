package com.butent.bee.client.calendar.monthview;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarKeeper;

public class AppointmentWidget extends FocusPanel {

  private final Appointment appointment;

  public AppointmentWidget(Appointment appointment) {
    this.appointment = appointment;

    Label label = new Label();
    CalendarKeeper.renderCompact(appointment, label);
    
    this.add(label);
  }

  public Appointment getAppointment() {
    return appointment;
  }
}
