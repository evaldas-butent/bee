package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.GwtEvent;

public class DeleteEvent<T> extends GwtEvent<DeleteHandler<T>> {

  private static Type<DeleteHandler<?>> TYPE;

  public static <T> boolean fire(HasDeleteHandlers<T> source, T target) {
    if (TYPE != null) {
      DeleteEvent<T> event = new DeleteEvent<T>(target);
      source.fireEvent(event);
      return !event.isCancelled();
    }
    return true;
  }

  public static Type<DeleteHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<DeleteHandler<?>>();
    }
    return TYPE;
  }

  private boolean cancelled = false;

  private final T target;

  protected DeleteEvent(T target) {
    this.target = target;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public final Type<DeleteHandler<T>> getAssociatedType() {
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
  protected void dispatch(DeleteHandler<T> handler) {
    handler.onDelete(this);
  }
}
