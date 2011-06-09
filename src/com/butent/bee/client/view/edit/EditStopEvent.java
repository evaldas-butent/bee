package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.State;

/**
 * Handles edit stop events for such reasons as error or cancellation.
 */

public class EditStopEvent extends GwtEvent<EditStopEvent.Handler> {

  /**
   * Requires implementing methods to have a method to handle edit stop.
   */

  public interface Handler extends EventHandler {
    void onEditStop(EditStopEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final State state;
  private final String message;

  public EditStopEvent(State state) {
    this(state, null);
  }

  public EditStopEvent(State state, String message) {
    this.state = state;
    this.message = message;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public String getMessage() {
    return message;
  }

  public State getState() {
    return state;
  }

  public boolean isCanceled() {
    return State.CANCELED.equals(getState());
  }

  public boolean isError() {
    return State.ERROR.equals(getState());
  }

  public boolean isFinished() {
    return State.CHANGED.equals(getState()) || State.CLOSED.equals(getState());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onEditStop(this);
  }
}
