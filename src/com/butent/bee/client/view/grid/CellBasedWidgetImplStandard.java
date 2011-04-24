package com.butent.bee.client.view.grid;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashSet;
import java.util.Set;

class CellBasedWidgetImplStandard extends CellBasedWidgetImpl {

  private static JavaScriptObject dispatchNonBubblingEvent;

  private static native void dispatchEvent(Event evt, Element elem, EventListener listener) /*-{
    @com.google.gwt.user.client.DOM::dispatchEvent(Lcom/google/gwt/user/client/Event;Lcom/google/gwt/user/client/Element;Lcom/google/gwt/user/client/EventListener;)(evt, elem, listener);
  }-*/;

  private static void handleNonBubblingEvent(Event event) {
    EventTarget eventTarget = event.getEventTarget();
    if (!Element.is(eventTarget)) {
      return;
    }
    com.google.gwt.user.client.Element target = eventTarget.cast();

    EventListener listener = DOM.getEventListener(target);
    while (target != null && listener == null) {
      target = target.getParentElement().cast();
      listener = (target == null) ? null : DOM.getEventListener(target);
    }

    if (listener != null) {
      dispatchEvent(event, target, listener);
    }
  }

  private final Set<String> nonBubblingEvents;

  public CellBasedWidgetImplStandard() {
    nonBubblingEvents = new HashSet<String>();
    nonBubblingEvents.add("focus");
    nonBubblingEvents.add("blur");
    nonBubblingEvents.add("load");
    nonBubblingEvents.add("error");
  }

  @Override
  public void onBrowserEvent(Widget widget, Event event) {
  }
  
  @Override
  protected int sinkEvent(Widget widget, String typeName) {
    if (nonBubblingEvents.contains(typeName)) {
      if (dispatchNonBubblingEvent == null) {
        initEventSystem();
      }

      Element elem = widget.getElement();
      String attr = "__beeCellBasedWidgetImplDispatching" + typeName;
      if (!"true".equals(elem.getAttribute(attr))) {
        elem.setAttribute(attr, "true");
        sinkEventImpl(elem, typeName);
      }
      return -1;
    } else {
      return super.sinkEvent(widget, typeName);
    }
  }

  private native void initEventSystem() /*-{
    @com.butent.bee.client.view.grid.CellBasedWidgetImplStandard::dispatchNonBubblingEvent = $entry(function(evt) {
      @com.butent.bee.client.view.grid.CellBasedWidgetImplStandard::handleNonBubblingEvent(Lcom/google/gwt/user/client/Event;)(evt);
    });
  }-*/;

  private native void sinkEventImpl(Element elem, String typeName) /*-{
    elem.addEventListener(typeName, @com.butent.bee.client.view.grid.CellBasedWidgetImplStandard::dispatchNonBubblingEvent, true);
  }-*/;
}
