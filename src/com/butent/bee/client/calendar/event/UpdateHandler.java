package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.EventHandler;

public interface UpdateHandler<T> extends EventHandler {
  void onUpdate(UpdateEvent<T> event);
}
