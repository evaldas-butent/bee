package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.client.view.View;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.logging.LogUtils;

public final class ReadyEvent extends GwtEvent<ReadyEvent.Handler> {

  public interface HasReadyHandlers extends HasHandlers {
    HandlerRegistration addReadyHandler(Handler handler);
  }

  public interface Handler extends EventHandler {
    void onReady(ReadyEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static void fire(HasReadyHandlers source) {
    Assert.notNull(source);
    if (source instanceof View) {
      LogUtils.getRootLogger().debug("ready", ((View) source).getId(),
          ((View) source).getElement().getClassName());
    }
    source.fireEvent(new ReadyEvent());
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  private ReadyEvent() {
    super();
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onReady(this);
  }
}
