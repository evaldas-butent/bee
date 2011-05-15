package com.butent.bee.client.view.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;

public class RowDeleteEvent extends Event<RowDeleteEvent.Handler> {
  
  public interface Handler {
    void onRowDelete(RowDeleteEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static HandlerRegistration register(Handler handler) {
    return BeeKeeper.getBus().addHandler(TYPE, handler);
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

  public String getViewName() {
    return viewName;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRowDelete(this);
  }
}
