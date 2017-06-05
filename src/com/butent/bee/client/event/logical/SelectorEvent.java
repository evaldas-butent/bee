package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.SelectionOracle.Callback;
import com.butent.bee.client.data.SelectionOracle.Request;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumable;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.function.Consumer;

public final class SelectorEvent extends GwtEvent<SelectorEvent.Handler> implements Consumable {

  @FunctionalInterface
  public interface Handler extends EventHandler {
    void onDataSelector(SelectorEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>();

  public static void fire(DataSelector selector, State state) {
    fireEvent(selector, new SelectorEvent(state));
  }

  public static SelectorEvent fireExclusions(DataSelector selector, Collection<Long> exclusions) {
    SelectorEvent event = new SelectorEvent(State.UPDATING);
    event.setExclusions(exclusions);

    fireEvent(selector, event);
    return event;
  }

  public static SelectorEvent fireNewRow(DataSelector selector, BeeRow row, String newRowFormName,
      String defValue) {

    SelectorEvent event = new SelectorEvent(State.NEW, row, newRowFormName);
    event.setDefValue(defValue);

    fireEvent(selector, event);
    return event;
  }

  public static SelectorEvent fireRequest(DataSelector selector, Request request,
      Callback callback) {

    SelectorEvent event = new SelectorEvent(State.PENDING);
    event.setRequest(request);
    event.setCallback(callback);

    fireEvent(selector, event);
    return event;
  }

  public static void fireRowCreated(DataSelector selector, BeeRow row) {
    SelectorEvent event = new SelectorEvent(State.CREATED, row);
    fireEvent(selector, event);
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
      event.revive();
      BeeKeeper.getBus().fireEventFromSource(event, selector);
    }
  }

  private final State state;
  private final BeeRow newRow;
  private String newRowFormName;

  private Collection<Long> exclusions;
  private Request request;
  private Callback callback;

  private boolean consumed;

  private String defValue;
  private Consumer<FormView> onOpenNewRow;

  private SelectorEvent(State state) {
    this(state, null, null);
  }

  private SelectorEvent(State state, BeeRow newRow) {
    this(state, newRow, null);
  }

  private SelectorEvent(State state, BeeRow newRow, String newRowFormName) {
    super();
    this.state = state;
    this.newRow = newRow;
    this.newRowFormName = newRowFormName;
  }

  @Override
  public void consume() {
    setConsumed(true);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Callback getCallback() {
    return callback;
  }

  public String getDefValue() {
    return defValue;
  }

  public Collection<Long> getExclusions() {
    return exclusions;
  }

  public BeeRow getNewRow() {
    return newRow;
  }

  public String getNewRowFormName() {
    return newRowFormName;
  }

  public Consumer<FormView> getOnOpenNewRow() {
    return onOpenNewRow;
  }

  public BeeRow getRelatedRow() {
    return (getSelector() == null) ? null : getSelector().getRelatedRow();
  }

  public String getRelatedViewName() {
    return (getSelector() == null) ? null : getSelector().getOracle().getViewName();
  }

  public Request getRequest() {
    return request;
  }

  public DataSelector getSelector() {
    if (!isLive()) {
      LogUtils.getRootLogger().warning(NameUtils.getName(this), "is dead");
      return null;
    } else if (getSource() instanceof DataSelector) {
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

  public boolean hasRelatedView(String viewName) {
    return getSelector() != null && getSelector().hasRelatedView(viewName);
  }

  public boolean isCanceled() {
    return State.CANCELED.equals(getState());
  }

  public boolean isChangePending() {
    return State.CHANGE_PENDING.equals(getState());
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

  public boolean isDataLoaded() {
    return State.LOADED.equals(getState());
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

  public boolean isRequest() {
    return State.PENDING.equals(getState());
  }

  public boolean isRowCreated() {
    return State.CREATED.equals(getState());
  }

  public boolean isUnloading() {
    return State.UNLOADING.equals(getState());
  }

  public boolean resumeRequest(DataSelector selector) {
    if (selector != null && getRequest() != null && getCallback() != null) {
      selector.getOracle().requestSuggestions(getRequest(), getCallback());
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void setConsumed(boolean consumed) {
    this.consumed = consumed;
  }

  public void setDefValue(String defValue) {
    this.defValue = defValue;
  }

  public void setNewRowFormName(String newRowFormName) {
    this.newRowFormName = newRowFormName;
  }

  public void setOnOpenNewRow(Consumer<FormView> onOpenNewRow) {
    this.onOpenNewRow = onOpenNewRow;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDataSelector(this);
  }

  private void setCallback(Callback callback) {
    this.callback = callback;
  }

  private void setExclusions(Collection<Long> exclusions) {
    this.exclusions = exclusions;
  }

  private void setRequest(Request request) {
    this.request = request;
  }
}
