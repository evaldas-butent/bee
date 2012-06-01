package com.butent.bee.client.calendar;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.impl.FocusImpl;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;

public abstract class InteractiveWidget extends Composite implements Focusable {

  private final FlowPanel rootPanel = new FlowPanel();

  private boolean lastWasKeyDown = false;

  public InteractiveWidget() {

    initWidget(rootPanel);

    sinkEvents(Event.ONMOUSEDOWN | Event.ONDBLCLICK | Event.KEYEVENTS);
    
    DomUtils.makeFocusable(rootPanel);

    Binder.addKeyDownHandler(rootPanel, new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        keyboardNavigation(event.getNativeEvent().getKeyCode());
        lastWasKeyDown = true;
      }
    });

    Binder.addKeyPressHandler(rootPanel, new KeyPressHandler() {
      public void onKeyPress(KeyPressEvent event) {
        if (!lastWasKeyDown) {
          keyboardNavigation(event.getNativeEvent().getKeyCode());
        }
        lastWasKeyDown = false;
      }
    });

    Binder.addKeyUpHandler(rootPanel, new KeyUpHandler() {
      public void onKeyUp(KeyUpEvent event) {
        lastWasKeyDown = false;
      }
    });
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
        onDoubleClick(element, event);
        setFocus(true);
        break;
      }

      case Event.ONMOUSEDOWN: {
        if (event.getButton() == NativeEvent.BUTTON_LEFT 
            && DOM.eventGetCurrentTarget(event) == getElement()) {
          onMouseDown(element, event);
          setFocus(true);
          DOM.eventCancelBubble(event, true);
          DOM.eventPreventDefault(event);
          return;
        }
        break;
      }

      case Event.ONMOUSEOVER: {
        if (DOM.eventGetCurrentTarget(event) == getElement()) {
          onMouseOver(element, event);
          DOM.eventCancelBubble(event, true);
          DOM.eventPreventDefault(event);
          return;
        }
      }
    }

    super.onBrowserEvent(event);
  }

  public abstract void onDoubleClick(Element element, Event event);

  public abstract void onDownArrowKeyPressed();

  public abstract void onLeftArrowKeyPressed();

  public abstract void onMouseDown(Element element, Event event);

  public abstract void onMouseOver(Element element, Event event);

  public abstract void onRightArrowKeyPressed();

  public abstract void onUpArrowKeyPressed();

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

  protected void keyboardNavigation(int key) {
    switch (key) {
      case KeyCodes.KEY_LEFT: {
        onLeftArrowKeyPressed();
        break;
      }
      case KeyCodes.KEY_UP: {
        onUpArrowKeyPressed();
        break;
      }
      case KeyCodes.KEY_RIGHT: {
        onRightArrowKeyPressed();
        break;
      }
      case KeyCodes.KEY_DOWN: {
        onDownArrowKeyPressed();
        break;
      }
    }
  }
}