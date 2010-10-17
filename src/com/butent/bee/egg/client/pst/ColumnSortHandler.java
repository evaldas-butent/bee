package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;

  public interface ColumnSortHandler extends EventHandler {

    /**
     * Called when a {@link ColumnSortEvent} is fired.
     * 
     * @param event the {@link ColumnSortEvent} that was fired
     */
    void onColumnSorted(ColumnSortEvent event);

}
