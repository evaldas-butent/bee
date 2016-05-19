package com.butent.bee.client.event;

import com.google.gwt.event.shared.EventHandler;

@FunctionalInterface
public interface InputHandler extends EventHandler {
  void onInput(InputEvent event);
}
