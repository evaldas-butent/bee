package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsRow;

public class ParentRowEvent extends Event<ParentRowEvent.Handler> {

  public interface Handler {
    void onParentRow(ParentRowEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }

  public static HandlerRegistration register(EventBus eventBus, Object source, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandlerToSource(TYPE, source, handler);
  }
  
  private final IsRow row;
  private final boolean enabled;

  public ParentRowEvent(IsRow row, boolean enabled) {
    super();
    this.row = row;
    this.enabled = enabled;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public IsRow getRow() {
    return row;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onParentRow(this);
  }
}
