package com.butent.bee.client.calendar.monthview;

import com.butent.bee.client.modules.calendar.Appointment;

public class MonthViewStyleManager {

  protected static final String APPOINTMENT_STYLE = "appointment";
  protected static final String APPOINTMENT_STYLE_SELECTED = "-selected";
  protected static final String APPOINTMENT_STYLE_MULTIDAY = "-multiday";

  protected static final String BACKGROUND_COLOR_STYLE_ATTRIBUTE = "backgroundColor";
  protected static final String BORDER_COLOR_STYLE_ATTRIBUTE = "borderColor";
  protected static final String COLOR_STYLE_ATTRIBUTE = "color";

  public MonthViewStyleManager() {
    super();
  }

  public void applyStyle(AppointmentWidget widget, boolean selected) {
    doApplyStyleInternal(widget, selected);
  }

  protected void doApplyStyleInternal(AppointmentWidget widget, boolean selected) {
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
