package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.GwtEvent;

public class DateRequestEvent<T> extends GwtEvent<DateRequestHandler<T>> {

  private static Type<DateRequestHandler<?>> TYPE;

  public static <T> void fire(HasDateRequestHandlers<T> source, T target) {
    fire(source, target, null);
  }

  public static <T> void fire(HasDateRequestHandlers<T> source, T target, Object widget) {
    if (TYPE != null) {
      DateRequestEvent<T> event = new DateRequestEvent<T>(target, widget);
      source.fireEvent(event);
    }
  }

  public static Type<DateRequestHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<DateRequestHandler<?>>();
    }
    return TYPE;
  }

  private final T target;
  private final Object clicked;

  protected DateRequestEvent(T target, Object clicked) {
    this.target = target;
    this.clicked = clicked;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public final Type<DateRequestHandler<T>> getAssociatedType() {
    return (Type) TYPE;
  }

  public Object getClicked() {
    return clicked;
  }

  public T getTarget() {
    return target;
  }

  @Override
  protected void dispatch(DateRequestHandler<T> handler) {
    handler.onDateRequested(this);
  }
}
