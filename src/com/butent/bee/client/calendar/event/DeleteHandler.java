package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.EventHandler;

public interface DeleteHandler<T> extends EventHandler {
  void onDelete(DeleteEvent<T> event);
}
