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
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class InteractiveWidget extends Composite {

  private FocusPanel focusPanel = new FocusPanel();

  protected FlowPanel rootPanel = new FlowPanel();

  private boolean lastWasKeyDown = false;

  public InteractiveWidget() {

    initWidget(rootPanel);

    sinkEvents(Event.ONMOUSEDOWN | Event.ONDBLCLICK | Event.KEYEVENTS);

    hideFocusPanel();

    focusPanel.addKeyPressHandler(new KeyPressHandler() {
      public void onKeyPress(KeyPressEvent event) {
        if (!lastWasKeyDown) {
          keyboardNavigation(event.getNativeEvent().getKeyCode());
        }
        lastWasKeyDown = false;
      }
    });

    focusPanel.addKeyUpHandler(new KeyUpHandler() {
      public void onKeyUp(KeyUpEvent event) {
        lastWasKeyDown = false;
      }
    });
    focusPanel.addKeyDownHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        keyboardNavigation(event.getNativeEvent().getKeyCode());
        lastWasKeyDown = true;
      }
    });
  }

  public ComplexPanel getRootPanel() {
    return rootPanel;
  }

  @Override
  public void onBrowserEvent(Event event) {
    int eventType = DOM.eventGetType(event);
    Element element = DOM.eventGetTarget(event);

    switch (eventType) {
      case Event.ONDBLCLICK: {
        onDoubleClick(element, event);
        focusPanel.setFocus(true);
        break;
      }

      case Event.ONMOUSEDOWN: {
        if (event.getButton() == NativeEvent.BUTTON_LEFT 
            && DOM.eventGetCurrentTarget(event) == getElement()) {
          onMouseDown(element, event);
          focusPanel.setFocus(true);
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

  public abstract void onDeleteKeyPressed();

  public abstract void onDoubleClick(Element element, Event event);

  public abstract void onDownArrowKeyPressed();

  public abstract void onLeftArrowKeyPressed();

  public abstract void onMouseDown(Element element, Event event);

  public abstract void onMouseOver(Element element, Event event);

  public abstract void onRightArrowKeyPressed();

  public abstract void onUpArrowKeyPressed();

  protected void keyboardNavigation(int key) {
    switch (key) {
      case KeyCodes.KEY_DELETE: {
        onDeleteKeyPressed();
        break;
      }
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

  private void hideFocusPanel() {
    RootPanel.get().add(focusPanel);
    DOM.setStyleAttribute(focusPanel.getElement(), "position", "absolute");
    DOM.setStyleAttribute(focusPanel.getElement(), "top", "-10");
    DOM.setStyleAttribute(focusPanel.getElement(), "left", "-10");
    DOM.setStyleAttribute(focusPanel.getElement(), "height", "0px");
    DOM.setStyleAttribute(focusPanel.getElement(), "width", "0px");
  }
}