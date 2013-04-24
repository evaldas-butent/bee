package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;

/**
 * Handles single row deletion event.
 */

public class RowDeleteEvent extends Event<RowDeleteEvent.Handler> implements DataEvent {

  /**
   * Requires implementing classes to have a method to handle single row deletion event.
   */

  public interface Handler {
    void onRowDelete(RowDeleteEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static HandlerRegistration register(EventBus eventBus, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandler(TYPE, handler);
  }

  private final String viewName;
  private final long rowId;

  public RowDeleteEvent(String viewName, long rowId) {
    this.viewName = viewName;
    this.rowId = rowId;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public long getRowId() {
    return rowId;
  }

  @Override
  public String getViewName() {
    return viewName;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRowDelete(this);
  }
}
