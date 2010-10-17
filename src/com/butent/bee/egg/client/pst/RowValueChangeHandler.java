package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;

public interface RowValueChangeHandler<RowType> extends EventHandler {
  void onRowValueChange(RowValueChangeEvent<RowType> event);
}
