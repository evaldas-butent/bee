package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.EventHandler;

public interface TimeBlockClickHandler<T> extends EventHandler {
  void onTimeBlockClick(TimeBlockClickEvent<T> event);
}
