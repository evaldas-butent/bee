package com.butent.bee.client.calendar.util;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

public class AppointmentWidget extends FlowPanel {

  private class Div extends ComplexPanel implements HasAllMouseHandlers {

    private Div() {
      setElement(DOM.createDiv());
    }

    public void add(Widget w) {
      super.add(w, getElement());
    }

    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
      return addDomHandler(handler, MouseDownEvent.getType());
    }

    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
      return addDomHandler(handler, MouseMoveEvent.getType());
    }

    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
      return addDomHandler(handler, MouseOutEvent.getType());
    }

    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
      return addDomHandler(handler, MouseOverEvent.getType());
    }

    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
      return addDomHandler(handler, MouseUpEvent.getType());
    }

    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
      return addDomHandler(handler, MouseWheelEvent.getType());
    }
  }

  private String title;
  private String description;

  private DateTime start;
  private DateTime end;
  
  private boolean multiDay = false;

  private boolean selected;
  
  private double top;
  private double left;
  private double width;
  private double height;

  private final Widget headerPanel = new Div();
  private final Panel bodyPanel = new SimplePanel();
  private final Widget footerPanel = new Div();

  private final Panel timelinePanel = new SimplePanel();
  private final Panel timelineFillPanel = new SimplePanel();
  
  private Appointment appointment;

  public AppointmentWidget() {
    setStylePrimaryName("bee-appointment");
    StyleUtils.makeAbsolute(this);
    
    headerPanel.setStylePrimaryName("header");
    bodyPanel.setStylePrimaryName("body");
    footerPanel.setStylePrimaryName("footer");
    
    timelinePanel.setStylePrimaryName("timeline");
    timelineFillPanel.setStylePrimaryName("timeline-fill");

    add(headerPanel);
    add(bodyPanel);
    add(footerPanel);
    
    add(timelinePanel);
    timelinePanel.add(timelineFillPanel);
  }

  public int compareTo(AppointmentWidget appt) {
    int compare = getStart().compareTo(appt.getStart());
    if (compare == 0) {
      compare = appt.getEnd().compareTo(getEnd());
    }
    return compare;
  }

  public void formatTimeline(double t, double h) {
    StyleUtils.setTop(timelineFillPanel, t, Unit.PCT);
    StyleUtils.setHeight(timelineFillPanel, h, Unit.PCT);
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public Widget getBody() {
    return bodyPanel;
  }

  public String getDescription() {
    return description;
  }

  public DateTime getEnd() {
    return end;
  }

  public Widget getHeader() {
    return headerPanel;
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

  public DateTime getStart() {
    return start;
  }

  public String getTitle() {
    return title;
  }

  public double getTop() {
    return top;
  }

  public double getWidth() {
    return width;
  }

  public boolean isMultiDay() {
    return multiDay;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
    
    setTitle(appointment.getSummary());

    setBackground(appointment.getBackground());
    setForeground(appointment.getForeground());
  }
  
  public void setBackground(String background) {
    if (!BeeUtils.isEmpty(background)) {
      getElement().getStyle().setBackgroundColor(background);
    }
  }

  public void setDescription(Appointment appointment) {
    String sep = "<br/>";

    String attNames = BeeConst.STRING_EMPTY;
    String propNames = BeeConst.STRING_EMPTY;
    
    if (!appointment.getAttendees().isEmpty()) {
      for (Long id : appointment.getAttendees()) {
        attNames = BeeUtils.concat(sep, attNames, CalendarKeeper.getAttendeeName(id));
      }
    }
    if (!appointment.getProperties().isEmpty()) {
      for (Long id : appointment.getProperties()) {
        propNames = BeeUtils.concat(sep, propNames, CalendarKeeper.getPropertyName(id));
      }
    }
    
    String text = BeeUtils.concat(sep, appointment.getCompanyName(),
        BeeUtils.concat(1, appointment.getVehicleNumber(), appointment.getVehicleParentModel(),
            appointment.getVehicleModel()), propNames, attNames, appointment.getDescription());

    this.description = text;
    DOM.setInnerHTML(bodyPanel.getElement(), text);
  }

  public void setEnd(DateTime end) {
    this.end = end;
  }

  public void setForeground(String foreground) {
    if (!BeeUtils.isEmpty(foreground)) {
      getElement().getStyle().setColor(foreground);
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

  public void setMultiDay(boolean isMultiDay) {
    this.multiDay = isMultiDay;
  }

  public void setStart(DateTime start) {
    this.start = start;
  }

  public void setTitle(String title) {
    this.title = title;
    DOM.setInnerHTML(headerPanel.getElement(), title);
  }

  public void setTop(double top) {
    this.top = top;
    StyleUtils.setTop(this, top, Unit.PX);
  }

  public void setWidth(double width) {
    this.width = width;
    StyleUtils.setWidth(this, width, Unit.PCT);
  }
}
