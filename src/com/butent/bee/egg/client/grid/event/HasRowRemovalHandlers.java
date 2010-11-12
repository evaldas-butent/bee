package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasRowRemovalHandlers extends HasHandlers {
  HandlerRegistration addRowRemovalHandler(RowRemovalHandler handler);
}
