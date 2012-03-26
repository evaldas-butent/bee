package com.butent.bee.client.calendar.monthview;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.ThemeAppointmentStyle;
import com.butent.bee.client.calendar.theme.DefaultTheme;

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
    Element elem = widget.getElement();
    boolean multiDay = appointment.isMultiDay() || appointment.isAllDay();

    ThemeAppointmentStyle style = getViewAppointmentStyleForTheme(appointment);

    String styleName = APPOINTMENT_STYLE;
    if (multiDay) {
      styleName += APPOINTMENT_STYLE_MULTIDAY;
    }
    if (selected) {
      styleName += APPOINTMENT_STYLE_SELECTED;
    }
    widget.setStylePrimaryName(styleName);

    if (style == null) {
      style = getDefaultViewAppointmentStyleForTheme();
    }

    if (multiDay) {
      DOM.setStyleAttribute(elem, BACKGROUND_COLOR_STYLE_ATTRIBUTE, style.getBackground());
      DOM.setStyleAttribute(elem, BORDER_COLOR_STYLE_ATTRIBUTE, style.getBorder());
    } else {
      DOM.setStyleAttribute(elem, COLOR_STYLE_ATTRIBUTE, style.getSelectedBorder());
    }

    if (selected) {
      DOM.setStyleAttribute(elem, BORDER_COLOR_STYLE_ATTRIBUTE, style.getSelectedBorder());
    }
  }

  protected ThemeAppointmentStyle getDefaultViewAppointmentStyleForTheme() {
    return DefaultTheme.DEFAULT;
  }

  protected ThemeAppointmentStyle getViewAppointmentStyleForTheme(Appointment appointment) {
    return DefaultTheme.STYLES.get(appointment.getStyle());
  }
}
