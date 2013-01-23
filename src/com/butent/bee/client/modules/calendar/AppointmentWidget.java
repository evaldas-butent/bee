package com.butent.bee.client.modules.calendar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.CssUnit;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;

public class AppointmentWidget extends Flow {

  private final Appointment appointment;
  private final boolean multi;
  private final int columnIndex;

  private double top;
  private double left;
  private double width;
  private double height;

  private final Mover headerPanel;
  private final Widget bodyPanel;
  private final Mover footerPanel;

  public AppointmentWidget(Appointment appointment, boolean multi) {
    this(appointment, multi, BeeConst.UNDEF, BeeConst.UNDEF, null);
  }
  
  public AppointmentWidget(Appointment appointment, boolean multi, int columnIndex, double height,
      Orientation footerOrientation) {

    this.appointment = appointment;
    this.multi = multi;
    this.columnIndex = columnIndex;

    String styleName;
    if (multi) {
      styleName = CalendarStyleManager.APPOINTMENT_MULTIDAY;
    } else if (height > 0 && height < 40) {
      styleName = CalendarStyleManager.APPOINTMENT_SMALL;
    } else if (height > 100) {
      styleName = CalendarStyleManager.APPOINTMENT_BIG;
    } else {
      styleName = CalendarStyleManager.APPOINTMENT;
    }

    addStyleName(styleName);

    this.headerPanel = new Mover(CalendarStyleManager.HEADER);
    this.bodyPanel = new CustomDiv(CalendarStyleManager.BODY);
    this.footerPanel = new Mover(CalendarStyleManager.FOOTER, footerOrientation);

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

  public Widget getBodyPanel() {
    return bodyPanel;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public Mover getCompactBar() {
    return footerPanel;
  }

  public Widget getFooterPanel() {
    return footerPanel;
  }

  public Widget getHeaderPanel() {
    return headerPanel;
  }

  public double getHeight() {
    return height;
  }

  public double getLeft() {
    return left;
  }

  public Mover getMoveHandle() {
    return headerPanel;
  }

  public Mover getResizeHandle() {
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

  public void render(long calendarId, String bg) {
    setBackground(BeeUtils.notEmpty(bg, appointment.getBackground()));
    setForeground(appointment.getForeground());

    CalendarKeeper.renderAppoinment(calendarId, this, multi);
  }

  public void renderCompact(long calendarId, String bg) {
    String background = BeeUtils.notEmpty(bg, appointment.getBackground());
    if (!BeeUtils.isEmpty(background)) {
      getCompactBar().getElement().getStyle().setBackgroundColor(background);
    }

    CalendarKeeper.renderCompact(calendarId, this, bodyPanel, this);
  }

  public void setBodyHtml(String html) {
    if (!BeeUtils.isEmpty(html)) {
      bodyPanel.getElement().setInnerHTML(BeeUtils.trim(html));
    }
  }

  public void setHeaderHtml(String html) {
    if (!BeeUtils.isEmpty(html)) {
      headerPanel.getElement().setInnerHTML(BeeUtils.trim(html));
    }
  }

  public void setHeight(double height) {
    this.height = height;
    StyleUtils.setHeight(this, height, CssUnit.PX);
  }

  public void setLeft(double left) {
    this.left = left;
    StyleUtils.setLeft(this, left, CssUnit.PCT);
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
    StyleUtils.setTop(this, top, CssUnit.PX);
  }

  public void setWidth(double width) {
    this.width = width;
    StyleUtils.setWidth(this, width, CssUnit.PCT);
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
