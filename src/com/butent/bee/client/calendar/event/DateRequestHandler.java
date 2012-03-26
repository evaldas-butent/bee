package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.EventHandler;

public interface DateRequestHandler<T> extends EventHandler {
  void onDateRequested(DateRequestEvent<T> event);
}