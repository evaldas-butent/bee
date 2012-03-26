package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasDateRequestHandlers<T> extends HasHandlers {
  HandlerRegistration addDateRequestHandler(DateRequestHandler<T> handler);
}
