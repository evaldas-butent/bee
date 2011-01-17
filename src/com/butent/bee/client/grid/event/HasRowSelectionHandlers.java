package com.butent.bee.client.grid.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasRowSelectionHandlers extends HasHandlers {
  HandlerRegistration addRowSelectionHandler(RowSelectionHandler handler);
}
