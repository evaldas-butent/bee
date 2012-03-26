package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.GwtEvent;

public class MouseOverEvent<T> extends GwtEvent<MouseOverHandler<T>> {

  private static Type<MouseOverHandler<?>> TYPE;

  public static <T> void fire(HasMouseOverHandlers<T> source, T target, Object element) {
    if (TYPE != null) {
      MouseOverEvent<T> event = new MouseOverEvent<T>(target, element);
      source.fireEvent(event);
    }
  }

  public static Type<MouseOverHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<MouseOverHandler<?>>();
    }
    return TYPE;
  }

  private final T target;

  private final Object element;

  protected MouseOverEvent(T target, Object element) {
    this.target = target;
    this.element = element;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public final Type<MouseOverHandler<T>> getAssociatedType() {
    return (Type) TYPE;
  }

  public Object getElement() {
    return element;
  }

  public T getTarget() {
    return target;
  }

  @Override
  protected void dispatch(MouseOverHandler<T> handler) {
    handler.onMouseOver(this);
  }
}
