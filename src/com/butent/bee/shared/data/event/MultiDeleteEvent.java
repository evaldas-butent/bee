package com.butent.bee.shared.data.event;

import com.google.common.collect.Sets;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;

import java.util.Collection;
import java.util.Set;

public class MultiDeleteEvent extends Event<MultiDeleteEvent.Handler> {
  
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
  private final Set<Long> rowIds;

  public MultiDeleteEvent(String viewName, Collection<Long> rowIds) {
    this.viewName = viewName;
    this.rowIds = Sets.newHashSet(rowIds); 
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Set<Long> getRowIds() {
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
