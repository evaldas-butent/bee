package com.butent.bee.shared.data.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.shared.Assert;

public class DataRequestEvent extends GwtEvent<DataRequestEvent.Handler> {

  public interface Handler extends EventHandler {
    void onDataRequest(DataRequestEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fire(HasHandlers source) {
    Assert.notNull(source);
    source.fireEvent(new DataRequestEvent());
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public DataRequestEvent() {
    super();
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDataRequest(this);
  }
}
