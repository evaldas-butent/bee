package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasCellUnhighlightHandlers {
  HandlerRegistration addCellUnhighlightHandler(CellUnhighlightHandler handler);
}
