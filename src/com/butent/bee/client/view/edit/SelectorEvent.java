package com.butent.bee.client.view.edit;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.shared.State;

public class SelectorEvent extends Event<SelectorEvent.Handler> {

  public interface Handler extends EventHandler {
    void onDataSelector(SelectorEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fire(DataSelector selector, State state) {
    BeeKeeper.getBus().fireEventFromSource(new SelectorEvent(state), selector);
  }
  
  public static Type<Handler> getType() {
    return TYPE;
  }

  public static HandlerRegistration register(DataSelector selector, Handler handler) {
    return BeeKeeper.getBus().addHandlerToSource(getType(), selector, handler);
  }
  
  public static HandlerRegistration register(Handler handler) {
    return BeeKeeper.getBus().addHandler(getType(), handler);
  }

  private final State state;

  public SelectorEvent(State state) {
    super();
    this.state = state;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }
  
  public String getRelatedViewName() {
    return (getSelector() == null) ? null : getSelector().getOracle().getViewName();
  }
  
  public DataSelector getSelector() {
    if (getSource() instanceof DataSelector) {
      return (DataSelector) getSource();
    } else {
      return null;
    }
  }

  public State getState() {
    return state;
  }
  
  public boolean isCanceled() {
    return State.CANCELED.equals(getState());
  }

  public boolean isChanged() {
    return State.CHANGED.equals(getState());
  }

  public boolean isClosed() {
    return State.CLOSED.equals(getState());
  }
  
  public boolean isOpened() {
    return State.OPEN.equals(getState());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDataSelector(this);
  }
}
