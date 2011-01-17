package com.butent.bee.client.grid.event;

import com.google.gwt.event.shared.EventHandler;

public interface RowSelectionHandler extends EventHandler {
  void onRowSelection(RowSelectionEvent event);
}
