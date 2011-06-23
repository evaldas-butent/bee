package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;

/**
 * Handles an event when a cell value is updated in table based user interface components.
 */

public class CellUpdateEvent extends Event<CellUpdateEvent.Handler> implements DataEvent {

  /**
   * Requires implementing classes to have a method to handle cell update event.
   */

  public interface Handler {
    void onCellUpdate(CellUpdateEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static HandlerRegistration register(EventBus eventBus, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandler(TYPE, handler);
  }

  private final String viewName;

  private final long rowId;
  private final long version;

  private final String columnId;
  private final String value;

  public CellUpdateEvent(String viewName, long rowId, long version, String columnId, String value) {
    this.viewName = viewName;
    this.rowId = rowId;
    this.version = version;
    this.columnId = columnId;
    this.value = value;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public String getColumnId() {
    return columnId;
  }

  public long getRowId() {
    return rowId;
  }

  public String getValue() {
    return value;
  }

  public long getVersion() {
    return version;
  }

  public String getViewName() {
    return viewName;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onCellUpdate(this);
  }
}
