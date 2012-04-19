package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.GwtEvent;

public class DeleteEvent<T> extends GwtEvent<DeleteHandler<T>> {

  private static Type<DeleteHandler<?>> TYPE;

  public static <T> boolean fire(HasDeleteHandlers<T> source, T target) {
    if (TYPE != null) {
      DeleteEvent<T> event = new DeleteEvent<T>(target);
      source.fireEvent(event);
      return !event.isCanceled();
    }
    return true;
  }

  public static Type<DeleteHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<DeleteHandler<?>>();
    }
    return TYPE;
  }

  private boolean canceled = false;

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

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  @Override
  protected void dispatch(DeleteHandler<T> handler) {
    handler.onDelete(this);
  }
}
