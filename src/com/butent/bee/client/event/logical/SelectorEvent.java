package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumable;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

public class SelectorEvent extends GwtEvent<SelectorEvent.Handler> implements Consumable {

  public interface Handler extends EventHandler {
    void onDataSelector(SelectorEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static void fire(DataSelector selector, State state) {
    fireEvent(selector, new SelectorEvent(state));
  }

  public static SelectorEvent fireExclusions(DataSelector selector, Collection<Long> exclusions) {
    SelectorEvent event = new SelectorEvent(State.UPDATING);
    event.setExclusions(exclusions);
    fireEvent(selector, event);
    return event;
  }

  public static SelectorEvent fireNewRow(DataSelector selector, IsRow row) {
    SelectorEvent event = new SelectorEvent(State.NEW, row);
    fireEvent(selector, event);
    return event;
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

  private static void fireEvent(DataSelector selector, SelectorEvent event) {
    selector.fireEvent(event);
    if (!event.isConsumed()) {
      BeeKeeper.getBus().fireEventFromSource(event, selector);
    }
  }
  
  private final State state;
  private final IsRow newRow;
  
  private Collection<Long> exclusions = null;

  private boolean consumed = false;

  private SelectorEvent(State state) {
    this(state, null);
  }

  private SelectorEvent(State state, IsRow newRow) {
    super();
    this.state = state;
    this.newRow = newRow;
  }

  @Override
  public void consume() {
    setConsumed(true);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Collection<Long> getExclusions() {
    return exclusions;
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

  @Override
  public boolean isConsumed() {
    return consumed;
  }

  public boolean isExclusions() {
    return State.UPDATING.equals(getState());
  }
  
  public boolean isNewRow() {
    return State.NEW.equals(getState());
  }

  public boolean isOpened() {
    return State.OPEN.equals(getState());
  }

  @Override
  public void setConsumed(boolean consumed) {
    this.consumed = consumed;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDataSelector(this);
  }

  private void setExclusions(Collection<Long> exclusions) {
    this.exclusions = exclusions;
  }
}
