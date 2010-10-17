package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.GwtEvent;

public class PagingFailureEvent extends GwtEvent<PagingFailureHandler> {
  private static Type<PagingFailureHandler> TYPE;

  public static void fire(HasPagingFailureHandlers source, Throwable exception) {
    if (TYPE != null) {
      PagingFailureEvent event = new PagingFailureEvent(exception);
      source.fireEvent(event);
    }
  }

  public static Type<PagingFailureHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<PagingFailureHandler>();
    }
    return TYPE;
  }
  
  private Throwable exception;

  public PagingFailureEvent(Throwable exception) {
    this.exception = exception;
  }
  
  @Override
  public Type<PagingFailureHandler> getAssociatedType() {
    return TYPE;
  }

  public Throwable getException() {
    return exception;
  }

  @Override
  protected void dispatch(PagingFailureHandler handler) {
    handler.onPagingFailure(this);
  }

}
