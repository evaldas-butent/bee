package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasUpdateHandlers<T> extends HasHandlers {
  HandlerRegistration addUpdateHandler(UpdateHandler<T> handler);
}
