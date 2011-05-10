package com.butent.bee.client.data;

import com.google.gwt.view.client.HasData;

import com.butent.bee.client.view.event.HasSortHandlers;
import com.butent.bee.shared.data.IsRow;

/**
 * Requires that implementing classes would have data rows and sort handlers.
 */

public interface HasDataTable extends HasData<IsRow>, HasSortHandlers {
  void setPageStart(int pageStart);

}
