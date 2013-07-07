package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public final class CatchEvent<T> extends GwtEvent<CatchEvent.CatchHandler<T>> {

  public interface HasCatchHandlers<T> extends HasHandlers {
    HandlerRegistration addCatchHandler(CatchHandler<T> handler);
  }

  public interface CatchHandler<T> extends EventHandler {
    void onCatch(CatchEvent<T> event);
  }

  private static final Type<CatchHandler<?>> TYPE = new Type<CatchHandler<?>>();

  public static <T> void fire(HasCatchHandlers<T> source, T packet, T destination) {
    if (TYPE != null) {
      CatchEvent<T> event = new CatchEvent<T>(packet, destination);
      source.fireEvent(event);
    }
  }

  public static Type<CatchHandler<?>> getType() {
    return TYPE;
  }

  private final T packet;
  private final T destination;

  private CatchEvent(T packet, T destination) {
    this.packet = packet;
    this.destination = destination;
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Type<CatchHandler<T>> getAssociatedType() {
    return (Type) TYPE;
  }

  public T getDestination() {
    return destination;
  }

  public T getPacket() {
    return packet;
  }

  @Override
  protected void dispatch(CatchHandler<T> handler) {
    handler.onCatch(this);
  }
}
