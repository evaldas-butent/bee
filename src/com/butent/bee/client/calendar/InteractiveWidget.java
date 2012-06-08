package com.butent.bee.client.calendar;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.impl.FocusImpl;

import com.butent.bee.client.dom.DomUtils;

public abstract class InteractiveWidget extends Composite implements Focusable {

  private final FlowPanel rootPanel = new FlowPanel();

  public InteractiveWidget() {
    initWidget(rootPanel);

    sinkEvents(Event.ONMOUSEDOWN | Event.ONDBLCLICK);
    
    DomUtils.makeFocusable(rootPanel);
  }

  public ComplexPanel getRootPanel() {
    return rootPanel;
  }

  public int getTabIndex() {
    return FocusImpl.getFocusImplForPanel().getTabIndex(getElement());
  }

  @Override
  public void onBrowserEvent(Event event) {
    int eventType = DOM.eventGetType(event);
    Element element = DOM.eventGetTarget(event);

    switch (eventType) {
      case Event.ONDBLCLICK: {
        setFocus(true);
        onDoubleClick(element, event);
        break;
      }

      case Event.ONMOUSEDOWN: {
        if (event.getButton() == NativeEvent.BUTTON_LEFT 
            && DOM.eventGetCurrentTarget(event) == getElement()) {
          setFocus(true);
          onMouseDown(element, event);
          DOM.eventCancelBubble(event, true);
          DOM.eventPreventDefault(event);
          return;
        }
        break;
      }
    }

    super.onBrowserEvent(event);
  }

  public abstract void onDoubleClick(Element element, Event event);

  public abstract void onMouseDown(Element element, Event event);

  public void setAccessKey(char key) {
    FocusImpl.getFocusImplForPanel().setAccessKey(getElement(), key);
  }

  public void setFocus(boolean focused) {
    if (focused) {
      FocusImpl.getFocusImplForPanel().focus(getElement());
    } else {
      FocusImpl.getFocusImplForPanel().blur(getElement());
    }
  }

  public void setTabIndex(int index) {
    FocusImpl.getFocusImplForPanel().setTabIndex(getElement(), index);
  }
  
  protected void clear() {
    getRootPanel().clear();
  }
}