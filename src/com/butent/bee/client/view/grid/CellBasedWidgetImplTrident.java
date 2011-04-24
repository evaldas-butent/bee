package com.butent.bee.client.view.grid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashSet;
import java.util.Set;

class CellBasedWidgetImplTrident extends CellBasedWidgetImpl {

  private static JavaScriptObject dispatchFocusEvent;

  private static Element focusedInput;

  private static boolean focusedInputChangesOnBlurOnly;

  private static Object focusedInputValue;

  private static Set<String> inputTypes;

  private static void dispatchCellEvent(Widget widget,
      com.google.gwt.user.client.Element target, int eventBits, Event event) {
    if (!widget.getElement().isOrHasChild(target)) {
      return;
    }

    DOM.setEventListener(target, widget);
    DOM.sinkEvents(target, eventBits);

    if (event != null) {
      target.dispatchEvent(event);
    }
  }

  private static native void dispatchEvent(Event evt, Element elem, EventListener listener) /*-{
    @com.google.gwt.user.client.DOM::dispatchEvent(Lcom/google/gwt/user/client/Event;Lcom/google/gwt/user/client/Element;Lcom/google/gwt/user/client/EventListener;)(evt, elem, listener);
  }-*/;

  private static Object getInputValue(Element elem) {
    if (isCheckbox(elem)) {
      return InputElement.as(elem).isChecked();
    }
    return getInputValueImpl(elem);
  }

  private static native String getInputValueImpl(Element elem) /*-{
    return elem.value;
  }-*/;

  @SuppressWarnings("unused")
  private static void handleNonBubblingEvent(Event event) {
    EventTarget eventTarget = event.getEventTarget();
    if (!Element.is(eventTarget)) {
      return;
    }
    final com.google.gwt.user.client.Element target = eventTarget.cast();

    com.google.gwt.user.client.Element curElem = target;
    EventListener listener = DOM.getEventListener(curElem);
    while (curElem != null && listener == null) {
      curElem = curElem.getParentElement().cast();
      listener = (curElem == null) ? null : DOM.getEventListener(curElem);
    }

    if (!(listener instanceof Widget)) {
      return;
    }
    Widget widget = (Widget) listener;

    if (target == widget.getElement()) {
      return;
    }

    String type = event.getType();
    if ("focusin".equals(type)) {
      String tagName = target.getTagName().toLowerCase();
      if (inputTypes.contains(tagName)) {
        focusedInput = target;
        focusedInputValue = getInputValue(target);
        focusedInputChangesOnBlurOnly = !"select".equals(tagName) && !isCheckbox(target);
      }

      dispatchCellEvent(widget, target, Event.ONFOCUS, null);
    } else if ("focusout".equals(type)) {
      maybeFireChangeEvent(widget);
      focusedInput = null;

      Event blurEvent = Document.get().createFocusEvent().cast();
      dispatchCellEvent(widget, target, Event.ONBLUR, null);
    } else if ("load".equals(type) || "error".equals(type)) {
      dispatchEvent(event, widget.getElement(), listener);
    }
  }

  private static boolean isCheckbox(Element elem) {
    if (elem == null || !"input".equalsIgnoreCase(elem.getTagName())) {
      return false;
    }
    String inputType = InputElement.as(elem).getType().toLowerCase();
    return "checkbox".equals(inputType) || "radio".equals(inputType);
  }

  private static void maybeFireChangeEvent(Widget widget) {
    if (focusedInput == null) {
      return;
    }

    Object newValue = getInputValue(focusedInput);
    if (!newValue.equals(focusedInputValue)) {
      focusedInputValue = newValue;

      com.google.gwt.user.client.Element target = focusedInput.cast();
      Event changeEvent = Document.get().createChangeEvent().cast();
      dispatchCellEvent(widget, target, Event.ONCHANGE, changeEvent);
    }
  }

  private final Set<String> changeEventTriggers;

  private boolean loadEventsInitialized;

  public CellBasedWidgetImplTrident() {
    if (inputTypes == null) {
      inputTypes = new HashSet<String>();
      inputTypes.add("select");
      inputTypes.add("input");
      inputTypes.add("textarea");
    }

    changeEventTriggers = new HashSet<String>();
    changeEventTriggers.add("mouseup");
    changeEventTriggers.add("mousewheel");
  }

  @Override
  public boolean isFocusable(Element elem) {
    return focusableTypes.contains(elem.getTagName().toLowerCase())
        || getTabIndexIfSpecified(elem) >= 0;
  }

  @Override
  public void onBrowserEvent(final Widget widget, Event event) {
    String type = event.getType().toLowerCase();
    if ("focus".equals(type) || "blur".equals(type) || "change".equals(type)) {
      EventTarget eventTarget = event.getEventTarget();
      if (Element.is(eventTarget)) {
        com.google.gwt.user.client.Element target = eventTarget.cast();
        if (target != widget.getElement()) {
          DOM.setEventListener(target, null);
        }
      }
    }

    if (focusedInput != null && "change".equals(type)) {
      focusedInputValue = getInputValue(focusedInput);
    }

    if (focusedInput != null && !focusedInputChangesOnBlurOnly
        && changeEventTriggers.contains(type)) {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        public void execute() {
          maybeFireChangeEvent(widget);
        }
      });
    }
  }

  @Override
  public SafeHtml processHtml(SafeHtml html) {
    if (loadEventsInitialized && html != null) {
      String moduleName = GWT.getModuleName();
      String listener = "__bee_CellBasedWidgetImplLoadListeners[\"" + moduleName + "\"]();";

      String htmlString = html.asString();
      htmlString = htmlString.replaceAll("(<img)([\\s/>])", "<img onload='"
          + listener + "' onerror='" + listener + "'$2");

      html = SafeHtmlUtils.fromTrustedString(htmlString);
    }
    return html;
  }

  @Override
  public void resetFocus(ScheduledCommand command) {
    Scheduler.get().scheduleDeferred(command);
  }

  @Override
  protected int sinkEvent(Widget widget, String typeName) {
    if ("change".equals(typeName) || "focus".equals(typeName) || "blur".equals(typeName)) {
      // Initialize the focus events.
      if (dispatchFocusEvent == null) {
        initFocusEventSystem();
      }

      int eventsToSink = 0;
      Element elem = widget.getElement();
      String attr = "__beeCellBasedWidgetImplDispatchingFocus";
      if (!"true".equals(elem.getAttribute(attr))) {
        elem.setAttribute(attr, "true");
        sinkFocusEvents(elem);

        for (String trigger : changeEventTriggers) {
          eventsToSink |= Event.getTypeInt(trigger);
        }
      }
      return eventsToSink;
    } else if ("load".equals(typeName) || "error".equals(typeName)) {
      if (!loadEventsInitialized) {
        loadEventsInitialized = true;
        initLoadEvents(GWT.getModuleName());
      }
      return -1;
    } else {
      return super.sinkEvent(widget, typeName);
    }
  }

  private native int getTabIndexIfSpecified(Element elem) /*-{
    var attrNode = elem.getAttributeNode('tabIndex');
    return (attrNode != null && attrNode.specified) ? elem.tabIndex : -1;
  }-*/;

  private native void initFocusEventSystem() /*-{
    @com.butent.bee.client.view.grid.CellBasedWidgetImplTrident::dispatchFocusEvent = $entry(function() {
      @com.butent.bee.client.view.grid.CellBasedWidgetImplTrident::handleNonBubblingEvent(Lcom/google/gwt/user/client/Event;)($wnd.event);
    });
  }-*/;

  private native void initLoadEvents(String moduleName) /*-{
    if (!$wnd.__bee_CellBasedWidgetImplLoadListeners) {
      $wnd.__bee_CellBasedWidgetImplLoadListeners = new Array();
    }

    $wnd.__bee_CellBasedWidgetImplLoadListeners[moduleName] = $entry(function() {
      @com.butent.bee.client.view.grid.CellBasedWidgetImplTrident::handleNonBubblingEvent(Lcom/google/gwt/user/client/Event;)($wnd.event);
    });
  }-*/;

  private native void sinkFocusEvents(Element elem) /*-{
    elem.attachEvent('onfocusin',
        @com.butent.bee.client.view.grid.CellBasedWidgetImplTrident::dispatchFocusEvent);
    elem.attachEvent('onfocusout',
        @com.butent.bee.client.view.grid.CellBasedWidgetImplTrident::dispatchFocusEvent);
  }-*/;
}
