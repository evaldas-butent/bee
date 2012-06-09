package com.butent.bee.client.modules.calendar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.utils.BeeUtils;

public class AppointmentWidget extends Flow implements HasAppointment {

  private final Appointment appointment;
  private final boolean multi;

  private double top;
  private double left;
  private double width;
  private double height;

  private final Widget headerPanel = new Html();
  private final Widget bodyPanel = new Html();
  private final Widget footerPanel = new Html();

  public AppointmentWidget(Appointment appointment, boolean multi) {
    this.appointment = appointment;
    this.multi = multi;

    StyleUtils.makeAbsolute(this);
    addStyleName(multi ? CalendarStyleManager.APPOINTMENT_MULTIDAY
        : CalendarStyleManager.APPOINTMENT);

    headerPanel.addStyleName(CalendarStyleManager.HEADER);
    bodyPanel.addStyleName(CalendarStyleManager.BODY);
    footerPanel.addStyleName(CalendarStyleManager.FOOTER);

    add(headerPanel);
    add(bodyPanel);
    add(footerPanel);
  }
  
  public boolean canClick(Element element) {
    if (element == null) {
      return false;
    } else if (multi) {
      return true;
    } else {
      return bodyPanel.getElement().isOrHasChild(element);
    }
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public double getHeight() {
    return height;
  }

  public double getLeft() {
    return left;
  }

  public Widget getMoveHandle() {
    return headerPanel;
  }

  public Widget getResizeHandle() {
    return footerPanel;
  }

  public double getTop() {
    return top;
  }

  public double getWidth() {
    return width;
  }
  
  public void render() {
    setBackground(appointment.getBackground());
    setForeground(appointment.getForeground());

    CalendarKeeper.renderAppoinment(this, multi);
  }

  public void renderCompact() {
    CalendarKeeper.renderCompact(appointment, headerPanel);
  }

  public void setBodyHtml(String html) {
    if (!BeeUtils.isEmpty(html)) {
      bodyPanel.getElement().setInnerHTML(html);
    }
  }

  public void setHeaderHtml(String html) {
    if (!BeeUtils.isEmpty(html)) {
      headerPanel.getElement().setInnerHTML(html);
    }
  }

  public void setHeight(double height) {
    this.height = height;
    StyleUtils.setHeight(this, height, Unit.PX);
  }

  public void setLeft(double left) {
    this.left = left;
    StyleUtils.setLeft(this, left, Unit.PCT);
  }

  public void setTitleText(String text) {
    if (!BeeUtils.isEmpty(text)) {
      if (multi) {
        setTitle(text);
      } else {
        bodyPanel.setTitle(text);
      }
    }
  }

  public void setTop(double top) {
    this.top = top;
    StyleUtils.setTop(this, top, Unit.PX);
  }

  public void setWidth(double width) {
    this.width = width;
    StyleUtils.setWidth(this, width, Unit.PCT);
  }

  private void setBackground(String background) {
    if (!BeeUtils.isEmpty(background)) {
      getElement().getStyle().setBackgroundColor(background);
    }
  }

  private void setForeground(String foreground) {
    if (!BeeUtils.isEmpty(foreground)) {
      getElement().getStyle().setColor(foreground);
    }
  }
}
