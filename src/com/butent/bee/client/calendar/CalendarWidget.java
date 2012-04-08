package com.butent.bee.client.calendar;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.calendar.event.CreateEvent;
import com.butent.bee.client.calendar.event.CreateHandler;
import com.butent.bee.client.calendar.event.DateRequestEvent;
import com.butent.bee.client.calendar.event.DateRequestHandler;
import com.butent.bee.client.calendar.event.DeleteEvent;
import com.butent.bee.client.calendar.event.DeleteHandler;
import com.butent.bee.client.calendar.event.HasDateRequestHandlers;
import com.butent.bee.client.calendar.event.HasDeleteHandlers;
import com.butent.bee.client.calendar.event.HasMouseOverHandlers;
import com.butent.bee.client.calendar.event.HasTimeBlockClickHandlers;
import com.butent.bee.client.calendar.event.HasUpdateHandlers;
import com.butent.bee.client.calendar.event.MouseOverEvent;
import com.butent.bee.client.calendar.event.MouseOverHandler;
import com.butent.bee.client.calendar.event.TimeBlockClickEvent;
import com.butent.bee.client.calendar.event.TimeBlockClickHandler;
import com.butent.bee.client.calendar.event.UpdateEvent;
import com.butent.bee.client.calendar.event.UpdateHandler;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasDateValue;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Collection;
import java.util.List;

public class CalendarWidget extends InteractiveWidget implements
    HasSelectionHandlers<Appointment>, HasDeleteHandlers<Appointment>,
    HasOpenHandlers<Appointment>, HasTimeBlockClickHandlers<HasDateValue>,
    HasUpdateHandlers<Appointment>, HasDateRequestHandlers<HasDateValue>,
    HasMouseOverHandlers<Appointment>,
    HasLayout, HasAppointments {

  private boolean layoutSuspended = false;

  private boolean layoutPending = false;

  private final JustDate date;

  private CalendarSettings settings = CalendarSettings.DEFAULT_SETTINGS;

  private AppointmentManager appointmentManager = null;

  private CalendarView view = null;

  public CalendarWidget() {
    this(TimeUtils.today());
  }

  public CalendarWidget(JustDate date) {
    super();
    appointmentManager = new AppointmentManager();
    this.date = date;
  }

  public void addAppointment(Appointment appointment) {
    Assert.notNull(appointment, "Added appointment cannot be null.");
    appointmentManager.addAppointment(appointment);
    refresh();
  }

  public void addAppointments(Collection<Appointment> appointments) {
    appointmentManager.addAppointments(appointments);
    refresh();
  }

  public HandlerRegistration addCreateHandler(CreateHandler<Appointment> handler) {
    return addHandler(handler, CreateEvent.getType());
  }

  public HandlerRegistration addDateRequestHandler(DateRequestHandler<HasDateValue> handler) {
    return addHandler(handler, DateRequestEvent.getType());
  }
  
  public void addDaysToDate(int numOfDays) {
    if (numOfDays != 0) {
      TimeUtils.addDay(date, numOfDays);
      refresh();
    }
  }
  
  public HandlerRegistration addDeleteHandler(DeleteHandler<Appointment> handler) {
    return addHandler(handler, DeleteEvent.getType());
  }

  public HandlerRegistration addMouseOverHandler(MouseOverHandler<Appointment> handler) {
    return addHandler(handler, MouseOverEvent.getType());
  }

  public HandlerRegistration addOpenHandler(OpenHandler<Appointment> handler) {
    return addHandler(handler, OpenEvent.getType());
  }

  public HandlerRegistration addSelectionHandler(SelectionHandler<Appointment> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  public HandlerRegistration addTimeBlockClickHandler(TimeBlockClickHandler<HasDateValue> handler) {
    return addHandler(handler, TimeBlockClickEvent.getType());
  }

  public void addToRootPanel(Widget widget) {
    getRootPanel().add(widget);
  }

  public HandlerRegistration addUpdateHandler(UpdateHandler<Appointment> handler) {
    return addHandler(handler, UpdateEvent.getType());
  }

  public void clearAppointments() {
    appointmentManager.clearAppointments();
    refresh();
  }

  public void doLayout() {
    view.doLayout();
  }

  public void doSizing() {
    view.doSizing();
  }

  public void fireCreateEvent(Appointment appointment) {
    boolean allow = CreateEvent.fire(this, appointment);
    if (!allow) {
      appointmentManager.rollback();
      refresh();
    }
  }

  public void fireDateRequestEvent(HasDateValue dt) {
    DateRequestEvent.fire(this, dt);
  }

  public void fireDateRequestEvent(HasDateValue dt, Element clicked) {
    DateRequestEvent.fire(this, dt, clicked);
  }

  public void fireDeleteEvent(Appointment appointment) {
    boolean allow = DeleteEvent.fire(this, appointment);
    if (allow) {
      appointmentManager.removeAppointment(appointment);
      refresh();
    }
  }

  public void fireMouseOverEvent(Appointment appointment, Element element) {
    if (appointment != null && !appointment.equals(appointmentManager.getHoveredAppointment())) {
      appointmentManager.setHoveredAppointment(appointment);
      MouseOverEvent.fire(this, appointment, element);
    }
  }

  public void fireOpenEvent(Appointment appointment) {
    OpenEvent.fire(this, appointment);
  }

  public void fireSelectionEvent(Appointment appointment) {
    view.onAppointmentSelected(appointment);
    SelectionEvent.fire(this, appointment);
  }

  public void fireTimeBlockClickEvent(HasDateValue dt) {
    TimeBlockClickEvent.fire(this, dt);
  }

  public void fireUpdateEvent(Appointment appointment) {
    refresh();

    boolean allow = UpdateEvent.fire(this, appointment);
    if (!allow) {
      appointmentManager.rollback();
      refresh();
    }
  }

  public List<Appointment> getAppointments() {
    return appointmentManager.getAppointments();
  }

  public JustDate getDate() {
    return JustDate.copyOf(date);
  }

  public int getDays() {
    return view == null ? 4 : view.getDisplayedDays();
  }

  public Appointment getSelectedAppointment() {
    return appointmentManager.getSelectedAppointment();
  }

  public CalendarSettings getSettings() {
    return this.settings;
  }

  public final CalendarView getView() {
    return view;
  }

  public boolean hasAppointmentSelected() {
    return appointmentManager.hasAppointmentSelected();
  }

  public boolean isTheSelectedAppointment(Appointment appointment) {
    return appointmentManager.isTheSelectedAppointment(appointment);
  }

  @Override
  public void onDeleteKeyPressed() {
    view.onDeleteKeyPressed();
  }

  @Override
  public void onDoubleClick(Element element, Event event) {
    view.onDoubleClick(element, event);
  }

  @Override
  public void onDownArrowKeyPressed() {
    view.onDownArrowKeyPressed();
  }

  @Override
  public void onLeftArrowKeyPressed() {
    view.onLeftArrowKeyPressed();
  }

  public void onLoad() {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        doSizing();
      }
    });
  }

  @Override
  public void onMouseDown(Element element, Event event) {
    view.onSingleClick(element, event);
  }

  public void onMouseOver(Element element, Event event) {
    view.onMouseOver(element, event);
  }

  @Override
  public void onRightArrowKeyPressed() {
    view.onRightArrowKeyPressed();
  }

  @Override
  public void onUpArrowKeyPressed() {
    view.onUpArrowKeyPressed();
  }

  public void refresh() {
    if (layoutSuspended) {
      layoutPending = true;
      return;
    }

    appointmentManager.resetHoveredAppointment();
    appointmentManager.sortAppointments();

    doLayout();
    doSizing();
  }

  public void removeAppointment(Appointment appointment) {
    removeAppointment(appointment, false);
  }

  public void removeAppointment(Appointment appointment, boolean fireEvents) {
    boolean commitChange = true;
    if (fireEvents) {
      commitChange = DeleteEvent.fire(this, getSelectedAppointment());
    }

    if (commitChange) {
      appointmentManager.removeAppointment(appointment);
      refresh();
    }
  }

  public void removeCurrentlySelectedAppointment() {
    appointmentManager.removeCurrentlySelectedAppointment();
  }

  public void resetSelectedAppointment() {
    appointmentManager.resetSelectedAppointment();
  }

  public void resumeLayout() {
    layoutSuspended = false;
    if (layoutPending) {
      refresh();
    }
  }

  public void scrollToHour(int hour) {
    view.scrollToHour(hour);
  }

  public boolean selectNextAppointment() {
    boolean selected = appointmentManager.selectNextAppointment();
    if (selected) {
      fireSelectionEvent(getSelectedAppointment());
    }
    return selected;
  }

  public boolean selectPreviousAppointment() {
    boolean selected = appointmentManager.selectPreviousAppointment();
    if (selected) {
      fireSelectionEvent(getSelectedAppointment());
    }
    return selected;
  }

  public void setCommittedAppointment(Appointment appt) {
    appointmentManager.setCommittedAppointment(appt);
  }

  public void setDate(JustDate newDate) {
    setDate(newDate, getDays());
  }
  
  public void setDate(JustDate newDate, int days) {
    Assert.notNull(newDate);
    if (newDate.equals(date) && days == view.getDisplayedDays()) {
      return;
    }
    
    date.setDate(newDate);
    view.setDisplayedDays(days);

    refresh();
  }

  public void setDays(int days) {
    view.setDisplayedDays(days);
    refresh();
  }
  
  public void setRollbackAppointment(Appointment appt) {
    appointmentManager.setRollbackAppointment(appt);
  }

  public void setSelectedAppointment(Appointment appointment) {
    setSelectedAppointment(appointment, true);
  }

  public void setSelectedAppointment(Appointment appointment, boolean fireEvents) {
    appointmentManager.setSelectedAppointment(appointment);
    if (fireEvents) {
      fireSelectionEvent(appointment);
    }
  }

  public void setSettings(CalendarSettings settings) {
    this.settings = settings;
  }

  public final void setView(CalendarView view) {
    this.getRootPanel().clear();
    this.view = view;
    this.view.attach(this);
    this.setStyleName(this.view.getStyleName());
    this.refresh();
  }

  public void suspendLayout() {
    layoutSuspended = true;
  }
}