package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.EventHandler;

public interface PagingFailureHandler extends EventHandler {
  void onPagingFailure(PagingFailureEvent event);
}
