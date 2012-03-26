package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasDeleteHandlers<T> extends HasHandlers {
  HandlerRegistration addDeleteHandler(DeleteHandler<T> handler);
}
