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
    KEYBOARD_ESCAPE, KEYBOARD_SAVE, MOUSE_CLOSE_BOX, MOUSE_OUTSIDE, SCRIPT
  }

  public interface Handler extends EventHandler {
    void onClose(CloseEvent event);
  }
  
  public interface HasCloseHandlers extends HasHandlers {
    HandlerRegistration addCloseHandler(Handler handler);
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

  public boolean actionCancel() {
    return keyboardEscape() || mouseEvent();
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

  public boolean isTarget(Element element) {
    if (element == null || eventTarget == null) {
      return false;
    } else {
      return EventUtils.equalsOrIsChild(element, eventTarget);
    }
  }
  
  public boolean keyboardEscape() {
    return Cause.KEYBOARD_ESCAPE.equals(cause);
  }

  public boolean keyboardEvent() {
    return keyboardEscape() || keyboardSave();
  }

  public boolean keyboardSave() {
    return Cause.KEYBOARD_SAVE.equals(cause);
  }

  public boolean mouseCloseBox() {
    return Cause.MOUSE_CLOSE_BOX.equals(cause);
  }
  
  public boolean mouseEvent() {
    return mouseCloseBox() || mouseOutside();
  }

  public boolean mouseOutside() {
    return Cause.MOUSE_OUTSIDE.equals(cause);
  }

  public boolean script() {
    return Cause.SCRIPT.equals(cause);
  }

  public boolean userCaused() {
    return keyboardEvent() || mouseEvent();
  }
  
  @Override
  protected void dispatch(Handler handler) {
    handler.onClose(this);
  }
}
