package com.butent.bee.client.grid.event;

import com.google.gwt.event.shared.EventHandler;

public interface ColumnSortHandler extends EventHandler {
  void onColumnSorted(ColumnSortEvent event);
}
