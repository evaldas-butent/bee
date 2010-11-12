package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.EventHandler;

public interface RowRemovalHandler extends EventHandler {
  void onRowRemoval(RowRemovalEvent event);
}
