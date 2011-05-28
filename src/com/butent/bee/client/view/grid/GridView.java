package com.butent.bee.client.view.grid;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.edit.HasEditEndHandlers;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;

import java.util.List;

/**
 * Specifies necessary methods for grid view user interface component.
 */

public interface GridView extends View, HasEditEndHandlers, NotificationListener {
  
  void applyOptions(String options);

  void create(List<BeeColumn> dataColumns, int rowCount, BeeRowSet rowSet);

  int estimatePageSize(int containerWidth, int containerHeight);

  Long getActiveRowId();
  
  HasDataTable getGrid();
  
  List<Long> getSelectedRows();  

  boolean isRowSelected(long rowId);
  
  void refreshCell(long rowId, String columnId);
  
  void updatePageSize(int pageSize, boolean init); 
}
