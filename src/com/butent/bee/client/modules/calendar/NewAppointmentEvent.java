package com.butent.bee.client.modules.calendar;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.data.IsRow;

public class NewAppointmentEvent extends Event<NewAppointmentEvent.Handler> {

  public interface Handler extends EventHandler {
    void onNewAppointment(NewAppointmentEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fire(IsRow row) {
    BeeKeeper.getBus().fireEvent(new NewAppointmentEvent(row));
  }
  
  public static Type<Handler> getType() {
    return TYPE;
  }

  public static HandlerRegistration register(Handler handler) {
    return BeeKeeper.getBus().addHandler(getType(), handler);
  }

  private final IsRow row;

  public NewAppointmentEvent(IsRow row) {
    super();
    this.row = row;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }
  
  public IsRow getRow() {
    return row;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onNewAppointment(this);
  }
}
