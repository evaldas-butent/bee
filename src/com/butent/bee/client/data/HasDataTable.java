package com.butent.bee.client.data;

import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.view.HasLoadingState;
import com.butent.bee.client.view.edit.HasEditState;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataRequestEvent;
import com.butent.bee.shared.data.event.HandlesDeleteEvents;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.HasActiveRowChangeHandlers;
import com.butent.bee.shared.data.event.HasSelectionCountChangeHandlers;
import com.butent.bee.shared.data.event.HasSortHandlers;
import com.butent.bee.shared.data.event.ScopeChangeEvent;

import java.util.List;

/**
 * Requires that implementing classes would have data rows and sort handlers.
 */

public interface HasDataTable extends HasLoadingState, HasSortHandlers,
    HandlesDeleteEvents, HandlesUpdateEvents, HasSelectionCountChangeHandlers, HasEditState,
    HasActiveRowChangeHandlers {

  HandlerRegistration addScopeChangeHandler(ScopeChangeEvent.Handler handler);
  
  HandlerRegistration addDataRequestHandler(DataRequestEvent.Handler handler);
  
  int getPageSize();
  
  int getPageStart();

  int getRowCount();

  List<? extends IsRow> getRowData();
  
  void refresh(boolean refreshChildren);
  
  void setPageSize(int pageSize, boolean fireScopeChange, boolean fireDataRequest);

  void setPageStart(int pageStart, boolean fireScopeChange, boolean fireDataRequest);

  void setRowCount(int count, boolean fireScopeChange);
  
  void setRowData(List<? extends IsRow> values, boolean refresh);
  
  void updateActiveRow(List<? extends IsRow> values);
}
