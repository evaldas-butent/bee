package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.GwtEvent;

public class TimeBlockClickEvent<T> extends GwtEvent<TimeBlockClickHandler<T>> {

  private static Type<TimeBlockClickHandler<?>> TYPE;

  public static <T> void fire(HasTimeBlockClickHandlers<T> source, T target) {
    if (TYPE != null) {
      TimeBlockClickEvent<T> event = new TimeBlockClickEvent<T>(target);
      source.fireEvent(event);
    }
  }

  public static Type<TimeBlockClickHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<TimeBlockClickHandler<?>>();
    }
    return TYPE;
  }

  private final T target;

  protected TimeBlockClickEvent(T target) {
    this.target = target;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public final Type<TimeBlockClickHandler<T>> getAssociatedType() {
    return (Type) TYPE;
  }

  public T getTarget() {
    return target;
  }

  @Override
  protected void dispatch(TimeBlockClickHandler<T> handler) {
    handler.onTimeBlockClick(this);
  }
}
