package com.butent.bee.client.event;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.Assert;

public class Binder {

  private static boolean initialized = false;
  
  private static JavaScriptObject dispatcher;

  public static HandlerRegistration addInputHandler(Widget widget, InputHandler handler) {
    Assert.notNull(handler, "addInputHandler: handler is null");
    
    sinkInput(widget);
    return widget.addHandler(handler, InputEvent.getType());
  }
  
  public static void sinkInput(Element elem) {
    Assert.notNull(elem, "sinkInput: element is null");
    maybeInitialize();
    sinkInputImpl(elem);
  }

  public static void sinkInput(UIObject obj) {
    Assert.notNull(obj, "sinkInput: object is null");
    sinkInput(obj.getElement());
  }
  
  private static native void initDispatcher() /*-{
    @com.butent.bee.client.event.Binder::dispatcher = $entry(function(evt) {
      var listener, curElem = this;
      while (curElem && !(listener = curElem.__listener)) {
        curElem = curElem.parentNode;
      }
      if (curElem && curElem.nodeType != 1) {
        curElem = null;
      }
      if (listener) {
        @com.google.gwt.user.client.DOM::dispatchEvent(Lcom/google/gwt/user/client/Event;Lcom/google/gwt/user/client/Element;Lcom/google/gwt/user/client/EventListener;)(evt, curElem, listener);
      }
    });
  }-*/;
  
  private static void maybeInitialize() {
    if (!initialized) {
      initDispatcher();
      initialized = true;
    }
  }

  private static native void sinkInputImpl(Element elem) /*-{
    elem.oninput = @com.butent.bee.client.event.Binder::dispatcher;
  }-*/;

  private Binder() {
  }
}
