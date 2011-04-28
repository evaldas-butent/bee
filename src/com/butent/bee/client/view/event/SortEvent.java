package com.butent.bee.client.view.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.shared.data.view.Order;

public class SortEvent extends GwtEvent<SortEvent.Handler> {

  public interface Handler extends EventHandler {
    void onSort(SortEvent event);
  }

  private static Type<Handler> TYPE;

  public static SortEvent fire(HasHandlers source, Order sortList) {
    SortEvent event = new SortEvent(sortList);
    if (TYPE != null) {
      source.fireEvent(event);
    }
    return event;
  }

  public static Type<Handler> getType() {
    if (TYPE == null) {
      TYPE = new Type<Handler>();
    }
    return TYPE;
  }

  private final Order order;

  protected SortEvent(Order order) {
    this.order = order;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Order getOrder() {
    return order;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSort(this);
  }
}
