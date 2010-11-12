package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasRowHighlightHandlers {
  HandlerRegistration addRowHighlightHandler(RowHighlightHandler handler);
}
