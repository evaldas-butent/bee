package com.butent.bee.client.event;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.Assert;

public class Binder {

  private static boolean initialized = false;
  
  private static JavaScriptObject dispatcher;

  public static HandlerRegistration addClickHandler(Widget widget, ClickHandler handler) {
    Assert.notNull(widget, "addClickHandler: widget is null");
    Assert.notNull(handler, "addClickHandler: handler is null");

    return widget.addDomHandler(handler, ClickEvent.getType());
  }
  
  public static HandlerRegistration addInputHandler(Widget widget, InputHandler handler) {
    Assert.notNull(handler, "addInputHandler: handler is null");
    
    sinkInput(widget);
    return widget.addHandler(handler, InputEvent.getType());
  }

  public static HandlerRegistration addKeyDownHandler(Widget widget, KeyDownHandler handler) {
    Assert.notNull(widget, "addKeyDownHandler: widget is null");
    Assert.notNull(handler, "addKeyDownHandler: handler is null");

    return widget.addDomHandler(handler, KeyDownEvent.getType());
  }

  public static HandlerRegistration addKeyPressHandler(Widget widget, KeyPressHandler handler) {
    Assert.notNull(widget, "addKeyPressHandler: widget is null");
    Assert.notNull(handler, "addKeyPressHandler: handler is null");

    return widget.addDomHandler(handler, KeyPressEvent.getType());
  }
  
  public static HandlerRegistration addKeyUpHandler(Widget widget, KeyUpHandler handler) {
    Assert.notNull(widget, "addKeyUpHandler: widget is null");
    Assert.notNull(handler, "addKeyUpHandler: handler is null");

    return widget.addDomHandler(handler, KeyUpEvent.getType());
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
