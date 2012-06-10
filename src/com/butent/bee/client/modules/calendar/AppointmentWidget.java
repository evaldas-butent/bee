package com.butent.bee.client.modules.calendar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

public class AppointmentWidget extends Flow implements HasAppointment {

  private final Appointment appointment;
  private final boolean multi;
  private final int columnIndex;

  private double top;
  private double left;
  private double width;
  private double height;

  private final Widget headerPanel = new Html();
  private final Widget bodyPanel = new Html();
  private final Widget footerPanel = new Html();
  
  private int dropRowIndex = BeeConst.UNDEF;
  private int dropColumnIndex = BeeConst.UNDEF;
  private int dropMinutes = BeeConst.UNDEF;

  public AppointmentWidget(Appointment appointment, boolean multi, int columnIndex) {
    this.appointment = appointment;
    this.multi = multi;
    this.columnIndex = columnIndex;

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

  public int getColumnIndex() {
    return columnIndex;
  }
  
  public Widget getCompactBar() {
    return footerPanel;
  }

  public int getDropColumnIndex() {
    return dropColumnIndex;
  }

  public int getDropMinutes() {
    return dropMinutes;
  }

  public int getDropRowIndex() {
    return dropRowIndex;
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

  public boolean isMulti() {
    return multi;
  }

  public void render() {
    setBackground(appointment.getBackground());
    setForeground(appointment.getForeground());

    CalendarKeeper.renderAppoinment(this, multi);
  }

  public void renderCompact() {
    String background = appointment.getBackground();
    if (!BeeUtils.isEmpty(background)) {
      getCompactBar().getElement().getStyle().setBackgroundColor(background);
    }

    CalendarKeeper.renderCompact(appointment, bodyPanel, this);
  }

  public void setBodyHtml(String html) {
    if (!BeeUtils.isEmpty(html)) {
      bodyPanel.getElement().setInnerHTML(BeeUtils.trim(html));
    }
  }

  public void setDropColumnIndex(int dropColumnIndex) {
    this.dropColumnIndex = dropColumnIndex;
  }

  public void setDropMinutes(int dropMinutes) {
    this.dropMinutes = dropMinutes;
  }

  public void setDropRowIndex(int dropRowIndex) {
    this.dropRowIndex = dropRowIndex;
  }

  public void setHeaderHtml(String html) {
    if (!BeeUtils.isEmpty(html)) {
      headerPanel.getElement().setInnerHTML(BeeUtils.trim(html));
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
        setTitle(BeeUtils.trim(text));
      } else {
        bodyPanel.setTitle(BeeUtils.trim(text));
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
