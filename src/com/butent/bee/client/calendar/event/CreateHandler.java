package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.EventHandler;

public interface CreateHandler<T> extends EventHandler {
  void onCreate(CreateEvent<T> event);
}
