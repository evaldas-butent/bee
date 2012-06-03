package com.butent.bee.client.calendar.dayview;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentWidget;

public class DayViewStyleManager {

  private static final String APPOINTMENT_STYLE = "dv-appointment";

  private static final String APPOINTMENT_STYLE_MULTIDAY = "-multiday";
  private static final String APPOINTMENT_STYLE_SELECTED = "-selected";

  public DayViewStyleManager() {
    super();
  }

  public void applyStyle(AppointmentWidget widget, boolean selected) {
    doApplyStyleInternal(widget, selected);
  }

  private void doApplyStyleInternal(AppointmentWidget widget, boolean selected) {
    Appointment appointment = widget.getAppointment();

    String styleName = APPOINTMENT_STYLE;
    if (appointment.isMultiDay()) {
      styleName += APPOINTMENT_STYLE_MULTIDAY;
    }
    if (selected) {
      styleName += APPOINTMENT_STYLE_SELECTED;
    }
    widget.setStylePrimaryName(styleName);
  }
}
