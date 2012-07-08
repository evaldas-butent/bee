package com.butent.bee.client.modules.calendar.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.State;

public class AppointmentEvent extends Event<AppointmentEvent.Handler> {

  public interface Handler extends EventHandler {
    void onAppointment(AppointmentEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fire(Appointment appointment, State state) {
    BeeKeeper.getBus().fireEvent(new AppointmentEvent(appointment, state));
  }
  
  public static Type<Handler> getType() {
    return TYPE;
  }

  public static HandlerRegistration register(Handler handler) {
    return BeeKeeper.getBus().addHandler(getType(), handler, false);
  }

  private final Appointment appointment;
  private final State state;

  public AppointmentEvent(Appointment appointment, State state) {
    super();
    this.appointment = appointment;
    this.state = state;
  }

  public Appointment getAppointment() {
    return appointment;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public State getState() {
    return state;
  }
  
  public boolean isNew() {
    return State.CREATED.equals(state);
  }

  public boolean isUpdated() {
    return State.CHANGED.equals(state);
  }
  
  @Override
  protected void dispatch(Handler handler) {
    handler.onAppointment(this);
  }
}
