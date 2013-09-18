package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Handles an event when a row value is updated in table based user interface components.
 */

public class RowUpdateEvent extends Event<RowUpdateEvent.Handler> implements DataEvent {

  /**
   * Requires implementing classes to have a method to handle row update event.
   */

  public interface Handler {
    void onRowUpdate(RowUpdateEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static HandlerRegistration register(EventBus eventBus, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandler(TYPE, handler);
  }

  private final String viewName;
  private final BeeRow row;

  public RowUpdateEvent(String viewName, BeeRow row) {
    this.viewName = viewName;
    this.row = row;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public BeeRow getRow() {
    return row;
  }

  public long getRowId() {
    return getRow().getId();
  }

  @Override
  public String getViewName() {
    return viewName;
  }

  @Override
  public boolean hasView(String view) {
    return BeeUtils.same(view, getViewName());
  }
  
  @Override
  protected void dispatch(Handler handler) {
    handler.onRowUpdate(this);
  }
}
