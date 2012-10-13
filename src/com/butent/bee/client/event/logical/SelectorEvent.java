package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class SelectorEvent extends Event<SelectorEvent.Handler> {

  public interface Handler extends EventHandler {
    void onDataSelector(SelectorEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fire(DataSelector selector, State state) {
    BeeKeeper.getBus().fireEventFromSource(new SelectorEvent(state), selector);
  }

  public static void fireNewRow(DataSelector selector, IsRow row) {
    BeeKeeper.getBus().fireEventFromSource(new SelectorEvent(State.NEW, row), selector);
  }
  
  public static Type<Handler> getType() {
    return TYPE;
  }

  public static HandlerRegistration register(DataSelector selector, Handler handler) {
    return BeeKeeper.getBus().addHandlerToSource(getType(), selector, handler, false);
  }
  
  public static HandlerRegistration register(Handler handler) {
    return BeeKeeper.getBus().addHandler(getType(), handler, false);
  }

  private final State state;
  private final IsRow newRow;

  public SelectorEvent(State state) {
    this(state, null);
  }

  public SelectorEvent(State state, IsRow newRow) {
    super();
    this.state = state;
    this.newRow = newRow;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public IsRow getNewRow() {
    return newRow;
  }

  public IsRow getRelatedRow() {
    return (getSelector() == null) ? null : getSelector().getRelatedRow();
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
  
  public long getValue() {
    if (BeeUtils.isLong(getSelector().getNormalizedValue())) {
      return BeeUtils.toLong(getSelector().getNormalizedValue());
    } else {
      return BeeConst.UNDEF;
    }
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

  public boolean isNewRow() {
    return State.NEW.equals(getState());
  }
  
  public boolean isOpened() {
    return State.OPEN.equals(getState());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDataSelector(this);
  }
}
