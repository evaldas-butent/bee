package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;

public interface RowSelectionHandler extends EventHandler {

  /**
   * Called when a {@link RowSelectionEvent} is fired.
   * 
   * @param event the {@link RowSelectionEvent} that was fired
   */
  void onRowSelection(RowSelectionEvent event);
}
