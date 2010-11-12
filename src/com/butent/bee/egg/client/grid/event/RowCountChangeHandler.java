package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.EventHandler;

public interface RowCountChangeHandler extends EventHandler {
  void onRowCountChange(RowCountChangeEvent event);
}
