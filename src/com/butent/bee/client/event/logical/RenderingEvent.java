package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.shared.Assert;

public final class RenderingEvent extends GwtEvent<RenderingEvent.Handler> {
  
  public interface HasRenderingHandlers extends HasHandlers {
    HandlerRegistration addRenderingHandler(Handler handler);
  }
  
  public interface Handler extends EventHandler {
    void onRender(RenderingEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fireAfter(HasRenderingHandlers source) {
    Assert.notNull(source);
    source.fireEvent(new RenderingEvent(true));
  }

  public static void fireBefore(HasRenderingHandlers source) {
    Assert.notNull(source);
    source.fireEvent(new RenderingEvent(false));
  }

  public static Type<Handler> getType() {
    return TYPE;
  }
  
  private final boolean after;
  
  private RenderingEvent(boolean after) {
    super();
    this.after = after;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public boolean isAfter() {
    return after;
  }

  public boolean isBefore() {
    return !after;
  }
  
  @Override
  protected void dispatch(Handler handler) {
    handler.onRender(this);
  }
}
