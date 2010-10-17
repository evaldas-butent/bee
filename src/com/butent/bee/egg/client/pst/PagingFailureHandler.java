package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;

public interface PagingFailureHandler extends EventHandler {

  /**
   * Called when a {@link PagingFailureEvent} is fired.
   * 
   * @param event the event that was fired
   */
  void onPagingFailure(PagingFailureEvent event);
}
