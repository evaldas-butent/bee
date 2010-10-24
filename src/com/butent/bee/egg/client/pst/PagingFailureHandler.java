package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;

public interface PagingFailureHandler extends EventHandler {
  void onPagingFailure(PagingFailureEvent event);
}
