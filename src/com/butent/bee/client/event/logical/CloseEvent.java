package com.butent.bee.client.event.logical;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.Assert;

public class CloseEvent extends GwtEvent<CloseEvent.Handler> {
  
  public enum Cause {
    KEYBOARD, MOUSE, SCRIPT
  }

  public interface HasCloseHandlers extends HasHandlers {
    HandlerRegistration addCloseHandler(Handler handler);
  }
  
  public interface Handler extends EventHandler {
    void onClose(CloseEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fire(HasCloseHandlers source, Cause cause, EventTarget eventTarget) {
    Assert.notNull(source);
    source.fireEvent(new CloseEvent(cause, eventTarget));
  }

  public static Type<Handler> getType() {
    return TYPE;
  }
  
  private final Cause cause;
  private final EventTarget eventTarget;

  public CloseEvent(Cause cause, EventTarget eventTarget) {
    super();
    this.cause = cause;
    this.eventTarget = eventTarget;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public Cause getCause() {
    return cause;
  }

  public EventTarget getEventTarget() {
    return eventTarget;
  }

  public boolean isKeyboard() {
    return Cause.KEYBOARD.equals(cause);
  }

  public boolean isMouse() {
    return Cause.MOUSE.equals(cause);
  }

  public boolean isScript() {
    return Cause.SCRIPT.equals(cause);
  }
  
  public boolean isTarget(Element element) {
    if (element == null || eventTarget == null) {
      return false;
    } else {
      return EventUtils.equalsOrIsChild(element, eventTarget);
    }
  }

  public boolean isUserCaused() {
    return isKeyboard() || isMouse();
  }
  
  @Override
  protected void dispatch(Handler handler) {
    handler.onClose(this);
  }
}
