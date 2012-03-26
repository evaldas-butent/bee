package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.EventHandler;

public interface MouseOverHandler<T> extends EventHandler {
  void onMouseOver(MouseOverEvent<T> event);
}
