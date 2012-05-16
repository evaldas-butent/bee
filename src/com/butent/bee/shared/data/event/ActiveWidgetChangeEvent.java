package com.butent.bee.shared.data.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

public class ActiveWidgetChangeEvent extends Event<ActiveWidgetChangeEvent.Handler> {

  public interface Handler extends EventHandler {
    void onActiveWidgetChange(ActiveWidgetChangeEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final String widgetId;
  private final boolean active;

  public ActiveWidgetChangeEvent(String widgetId, boolean active) {
    super();
    this.widgetId = widgetId;
    this.active = active;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public String getWidgetId() {
    return widgetId;
  }

  public boolean isActive() {
    return active;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onActiveWidgetChange(this);
  }
}
