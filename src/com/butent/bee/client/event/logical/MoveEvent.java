package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.shared.Assert;

public class MoveEvent extends GwtEvent<MoveEvent.Handler> {
  
  public interface Handler extends EventHandler {
    void onMove(MoveEvent event);
  }

  public interface HasMoveHandlers extends HasHandlers {
    HandlerRegistration addMoveHandler(Handler handler);
  }
  
  private enum State {
    MOVING, FINISHED
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fireFinish(HasMoveHandlers source, int delta) {
    Assert.notNull(source);
    source.fireEvent(new MoveEvent(State.FINISHED, delta));
  }

  public static void fireMove(HasMoveHandlers source, int delta) {
    Assert.notNull(source);
    source.fireEvent(new MoveEvent(State.MOVING, delta));
  }
  
  public static Type<Handler> getType() {
    return TYPE;
  }
  
  private final State state;
  private final int delta;

  private MoveEvent(State state, int delta) {
    super();
    this.state = state;
    this.delta = delta;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public int getDelta() {
    return delta;
  }

  public boolean isFinished() {
    return State.FINISHED.equals(state);
  }
  
  public boolean isMoving() {
    return State.MOVING.equals(state);
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onMove(this);
  }
}
