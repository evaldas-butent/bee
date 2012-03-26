package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.GwtEvent;

public class UpdateEvent<T> extends GwtEvent<UpdateHandler<T>> {

  private static Type<UpdateHandler<?>> TYPE;

  public static <T> boolean fire(HasUpdateHandlers<T> source, T target) {
    if (TYPE != null) {
      UpdateEvent<T> event = new UpdateEvent<T>(target);
      source.fireEvent(event);
      return !event.isCancelled();
    }
    return true;
  }

  public static Type<UpdateHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<UpdateHandler<?>>();
    }
    return TYPE;
  }

  private boolean cancelled = false;

  private final T target;

  protected UpdateEvent(T target) {
    this.target = target;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public final Type<UpdateHandler<T>> getAssociatedType() {
    return (Type) TYPE;
  }

  public T getTarget() {
    return target;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  @Override
  protected void dispatch(UpdateHandler<T> handler) {
    handler.onUpdate(this);
  }
}
