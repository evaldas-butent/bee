package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRow;

/**
 * Handles an event when a row value is inserted in table based user interface components.
 */

public class RowInsertEvent extends Event<RowInsertEvent.Handler> implements DataEvent {

  /**
   * Requires implementing classes to have a method to handle row insert event.
   */

  public interface Handler {
    void onRowInsert(RowInsertEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static HandlerRegistration register(EventBus eventBus, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandler(TYPE, handler);
  }

  private final String viewName;
  private final BeeRow row;

  public RowInsertEvent(String viewName, BeeRow row) {
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

  public String getViewName() {
    return viewName;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRowInsert(this);
  }
}
