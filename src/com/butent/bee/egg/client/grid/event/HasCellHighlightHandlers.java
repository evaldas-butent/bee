package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasCellHighlightHandlers {
  HandlerRegistration addCellHighlightHandler(CellHighlightHandler handler);
}
