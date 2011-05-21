package com.butent.bee.client.data;

import com.google.gwt.view.client.HasData;

import com.butent.bee.client.view.HasLoadingState;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.HandlesDeleteEvents;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.HasSelectionCountChangeHandlers;
import com.butent.bee.shared.data.event.HasSortHandlers;

import java.util.List;

/**
 * Requires that implementing classes would have data rows and sort handlers.
 */

public interface HasDataTable extends HasData<IsRow>, HasLoadingState, HasSortHandlers,
    HandlesDeleteEvents, HandlesUpdateEvents, HasSelectionCountChangeHandlers {

  void setPageSize(int pageSize);

  void setPageStart(int pageStart);

  void updateActiveRow(List<? extends IsRow> values);
}
