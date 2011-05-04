package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.KeyPressEvent;

/**
 * Requires implementing classes to have a method to handle keyboard key presses.
 */

public interface HasBeeKeyHandler {
  boolean onBeeKey(KeyPressEvent event);

}
