package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.GwtEvent;

public class CreateEvent<T> extends GwtEvent<CreateHandler<T>> {

  private static Type<CreateHandler<?>> TYPE;

  public static <T> boolean fire(HasUpdateHandlers<T> source, T target) {
    if (TYPE != null) {
      CreateEvent<T> event = new CreateEvent<T>(target);
      source.fireEvent(event);
      return !event.isCanceled();
    }
    return true;
  }

  public static Type<CreateHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<CreateHandler<?>>();
    }
    return TYPE;
  }

  private boolean canceled = false;

  private final T target;

  protected CreateEvent(T target) {
    this.target = target;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public final Type<CreateHandler<T>> getAssociatedType() {
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
  protected void dispatch(CreateHandler<T> handler) {
    handler.onCreate(this);
  }
}
