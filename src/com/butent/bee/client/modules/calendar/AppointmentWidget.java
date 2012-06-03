package com.butent.bee.client.modules.calendar;

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
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.widget.Html;
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

  private final Appointment appointment;
  private final boolean multi;

  private double top;
  private double left;
  private double width;
  private double height;

  private final Widget headerPanel = new Div();
  private final Widget bodyPanel = new Html();
  private final Widget footerPanel = new Div();

  public AppointmentWidget(Appointment appointment, boolean multi) {
    this.appointment = appointment;
    this.multi = multi;

    setStylePrimaryName("bee-appointment");
    StyleUtils.makeAbsolute(this);
    
    headerPanel.setStylePrimaryName("header");
    bodyPanel.setStylePrimaryName("body");
    footerPanel.setStylePrimaryName("footer");
    
    add(headerPanel);
    add(bodyPanel);
    add(footerPanel);
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
