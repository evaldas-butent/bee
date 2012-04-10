package com.butent.bee.client.calendar.dayview;

import com.google.gwt.user.client.Element;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.ThemeAppointmentStyle;
import com.butent.bee.client.calendar.theme.DefaultTheme;
import com.butent.bee.client.calendar.util.AppointmentWidget;
import com.butent.bee.client.dom.StyleUtils;

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

    Element elem = widget.getElement();

    Element headerElem = widget.getHeader().getElement();
    Element bodyElem = widget.getBody().getElement();

    boolean multiDay = appointment.isMultiDay() || appointment.isAllDay();

    String styleName = APPOINTMENT_STYLE;
    if (multiDay) {
      styleName += APPOINTMENT_STYLE_MULTIDAY;
    }
    if (selected) {
      styleName += APPOINTMENT_STYLE_SELECTED;
    }
    widget.setStylePrimaryName(styleName);

    ThemeAppointmentStyle style = getViewAppointmentStyleForTheme(appointment);
    if (style == null) {
      style = getDefaultViewAppointmentStyleForTheme();
    }

    if (multiDay) {
      StyleUtils.setBackgroundColor(elem, style.getBackgroundHeader());
    } else {
      StyleUtils.setBackgroundColor(elem, style.getBackground());
    }

    StyleUtils.setBorderColor(elem, style.getBackgroundHeader());

    StyleUtils.setBackgroundColor(headerElem, style.getBackgroundHeader());
    StyleUtils.setColor(headerElem, style.getHeaderText());
    
    StyleUtils.setColor(bodyElem, style.getSelectedBorder());
  }

  private ThemeAppointmentStyle getDefaultViewAppointmentStyleForTheme() {
    return DefaultTheme.DEFAULT;
  }
  
  private ThemeAppointmentStyle getViewAppointmentStyleForTheme(Appointment appointment) {
    return DefaultTheme.STYLES.get(appointment.getStyle());
  }
}
