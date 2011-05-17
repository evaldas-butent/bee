package com.butent.bee.client.view.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;

import java.util.Collection;

/**
 * Enables deleting more than one row of data.
 */

public class MultiDeleteEvent extends Event<MultiDeleteEvent.Handler> {

  /**
   * Requires any implementing classes to have a {@code onMultiDelete} method.
   */

  public interface Handler {
    void onMultiDelete(MultiDeleteEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static HandlerRegistration register(Handler handler) {
    return BeeKeeper.getBus().addHandler(TYPE, handler);
  }

  private final String viewName;
  private final Collection<Long> rowIds;

  public MultiDeleteEvent(String viewName, Collection<Long> rowIds) {
    this.viewName = viewName;
    this.rowIds = rowIds;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Collection<Long> getRowIds() {
    return rowIds;
  }

  public String getViewName() {
    return viewName;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onMultiDelete(this);
  }
}
