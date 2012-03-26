package com.butent.bee.client.calendar.dayview;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.ThemeAppointmentStyle;
import com.butent.bee.client.calendar.theme.DefaultTheme;

public class DayViewStyleManager {

  protected static final String APPOINTMENT_STYLE = "dv-appointment";

  protected static final String APPOINTMENT_STYLE_SELECTED = "-selected";

  protected static final String APPOINTMENT_STYLE_MULTIDAY = "-multiday";

  protected static final String BACKGROUND_COLOR_STYLE_ATTRIBUTE = "backgroundColor";

  protected static final String BACKGROUND_IMAGE_STYLE_ATTRIBUTE = "backgroundImage";

  protected static final String BORDER_COLOR_STYLE_ATTRIBUTE = "borderColor";

  protected static final String COLOR_STYLE_ATTRIBUTE = "color";

  public DayViewStyleManager() {
    super();
  }

  public void applyStyle(AppointmentWidget widget, boolean selected) {
    doApplyStyleInternal(widget, selected);
  }

  protected ThemeAppointmentStyle getDefaultViewAppointmentStyleForTheme() {
    return DefaultTheme.DEFAULT;
  }

  protected ThemeAppointmentStyle getViewAppointmentStyleForTheme(Appointment appointment) {
    return DefaultTheme.STYLES.get(appointment.getStyle());
  }
  
  private void doApplyStyleInternal(AppointmentWidget widget, boolean selected) {
    Appointment appointment = widget.getAppointment();

    Element elem = widget.getElement();
    Element bodyElem = widget.getBody().getElement();
    Element headerElem = widget.getHeader().getElement();

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
      DOM.setStyleAttribute(elem, BACKGROUND_COLOR_STYLE_ATTRIBUTE, style.getBackgroundHeader());
    } else {
      DOM.setStyleAttribute(elem, BACKGROUND_COLOR_STYLE_ATTRIBUTE, style.getBackground());
    }

    DOM.setStyleAttribute(elem, BORDER_COLOR_STYLE_ATTRIBUTE, style.getBackgroundHeader());

    DOM.setStyleAttribute(bodyElem, COLOR_STYLE_ATTRIBUTE, style.getSelectedBorder());

    DOM.setStyleAttribute(headerElem, COLOR_STYLE_ATTRIBUTE, style.getHeaderText());

    DOM.setStyleAttribute(headerElem, BACKGROUND_COLOR_STYLE_ATTRIBUTE, style.getBackgroundHeader());

    if (multiDay) {
      return;
    }
  }
}
