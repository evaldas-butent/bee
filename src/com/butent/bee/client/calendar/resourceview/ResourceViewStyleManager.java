package com.butent.bee.client.calendar.resourceview;

import com.butent.bee.client.calendar.util.AppointmentWidget;
import com.butent.bee.client.modules.calendar.Appointment;

public class ResourceViewStyleManager {

  private static final String APPOINTMENT_STYLE = "dv-appointment";

  private static final String APPOINTMENT_STYLE_MULTIDAY = "-multiday";
  private static final String APPOINTMENT_STYLE_SELECTED = "-selected";

  public ResourceViewStyleManager() {
    super();
  }

  public void applyStyle(AppointmentWidget widget, boolean selected) {
    doApplyStyleInternal(widget, selected);
  }

  private void doApplyStyleInternal(AppointmentWidget widget, boolean selected) {
    Appointment appointment = widget.getAppointment();

    boolean multiDay = appointment.isMultiDay() || appointment.isAllDay();

    String styleName = APPOINTMENT_STYLE;
    if (multiDay) {
      styleName += APPOINTMENT_STYLE_MULTIDAY;
    }
    if (selected) {
      styleName += APPOINTMENT_STYLE_SELECTED;
    }
    widget.setStylePrimaryName(styleName);
  }
}
