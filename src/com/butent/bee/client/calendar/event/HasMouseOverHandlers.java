package com.butent.bee.client.calendar.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasMouseOverHandlers<T> extends HasHandlers {
  HandlerRegistration addMouseOverHandler(MouseOverHandler<T> handler);
}
