package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;

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

  private final CellSource source;

  private final String value;

  public CellUpdateEvent(String viewName, long rowId, long version, CellSource source,
      String value) {
    this.viewName = viewName;

    this.rowId = rowId;
    this.version = version;

    this.source = Assert.notNull(source);
    
    this.value = value;
  }
  
  public boolean applyTo(BeeRowSet rowSet) {
    Assert.notNull(rowSet);
    
    IsRow row = rowSet.getRowById(getRowId());
    if (row == null) {
      return false;
    } else {
      applyTo(row);
      return true;
    }
  }

  public void applyTo(IsRow row) {
    Assert.notNull(row);
    row.setVersion(getVersion());
    
    source.set(row, value);
  }
  
  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public CellSource getCellSource() {
    return source;
  }

  public long getRowId() {
    return rowId;
  }

  public String getSourceName() {
    return source.getName();
  }

  public long getVersion() {
    return version;
  }

  @Override
  public String getViewName() {
    return viewName;
  }

  public boolean hasColumn() {
    return source.hasColumn();
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onCellUpdate(this);
  }
}
