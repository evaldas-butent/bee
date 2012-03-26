package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasTimeBlockClickHandlers<T> extends HasHandlers {
  HandlerRegistration addTimeBlockClickHandler(TimeBlockClickHandler<T> handler);
}
