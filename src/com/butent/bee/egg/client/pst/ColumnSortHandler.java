package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;

public interface ColumnSortHandler extends EventHandler {
  void onColumnSorted(ColumnSortEvent event);
}
