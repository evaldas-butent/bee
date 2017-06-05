package com.butent.bee.client.modules.calendar.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.shared.time.DateTime;

public final class CopyEvent extends GwtEvent<CopyEvent.Handler> {

  public interface Handler extends EventHandler {
    void onCopy(CopyEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>();

  public static boolean fire(HasUpdateHandlers source, Appointment appointment,
      DateTime newStart, DateTime newEnd) {
    CopyEvent event = new CopyEvent(appointment, newStart, newEnd);
    source.fireEvent(event);
    return !event.isCanceled();
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final Appointment appointment;

  private final DateTime newStart;
  private final DateTime newEnd;

  private boolean canceled;

  private CopyEvent(Appointment appointment, DateTime newStart, DateTime newEnd) {
    super();
    this.appointment = appointment;
    this.newStart = newStart;
    this.newEnd = newEnd;
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public DateTime getNewStart() {
    return newStart;
  }

  public DateTime getNewEnd() {
    return newEnd;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onCopy(this);
  }
}
