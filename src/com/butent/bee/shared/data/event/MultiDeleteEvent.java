package com.butent.bee.shared.data.event;

import com.google.common.collect.Sets;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.view.RowInfo;

import java.util.Collection;
import java.util.Set;

/**
 * Handles deletion of multiple rows event.
 */

public class MultiDeleteEvent extends Event<MultiDeleteEvent.Handler> implements DataEvent {

  /**
   * Requires implementing classes to have a method to handle multiple row deletion event.
   */

  public interface Handler {
    void onMultiDelete(MultiDeleteEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static HandlerRegistration register(EventBus eventBus, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandler(TYPE, handler);
  }

  private final String viewName;
  private final Set<RowInfo> rows;

  public MultiDeleteEvent(String viewName, Collection<RowInfo> rows) {
    this.viewName = viewName;
    this.rows = Sets.newHashSet(rows);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Set<RowInfo> getRows() {
    return rows;
  }
  
  public int getSize() {
    return rows.size();
  }

  public String getViewName() {
    return viewName;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onMultiDelete(this);
  }
}
