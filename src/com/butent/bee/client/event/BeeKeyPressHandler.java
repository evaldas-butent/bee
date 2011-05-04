package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * Handles keyboard key presses events.
 */
public class BeeKeyPressHandler implements KeyPressHandler {

  public void onKeyPress(KeyPressEvent event) {
    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      Object source = event.getSource();

      if (source instanceof HasBeeKeyHandler) {
        boolean ok = ((HasBeeKeyHandler) source).onBeeKey(event);
        if (!ok && source instanceof ValueBoxBase<?>) {
          ((ValueBoxBase<?>) source).cancelKey();
        }
      }
    }
  }

}
