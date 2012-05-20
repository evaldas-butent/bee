package com.butent.bee.client.calendar.monthview;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.modules.calendar.Appointment;

public class AppointmentWidget extends FocusPanel {

  private Appointment appointment;

  public AppointmentWidget(Appointment appointment) {
    this.appointment = appointment;
    Label titleLabel = new Label();
    titleLabel.getElement().setInnerHTML(this.appointment.getTitle());
    this.add(titleLabel);
  }

  public Appointment getAppointment() {
    return appointment;
  }
}
