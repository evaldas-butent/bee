package com.butent.bee.client.view.grid;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.edit.HasEditEndHandlers;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;
import java.util.List;

/**
 * Specifies necessary methods for grid view user interface component.
 */

public interface GridView extends View, HasEditEndHandlers, NotificationListener {
  
  void addRow();
  
  void applyOptions(String options);

  void create(List<BeeColumn> dataColumns, int rowCount, BeeRowSet rowSet,
      GridDescription gridDescription, boolean hasSearch);

  int estimatePageSize(int containerWidth, int containerHeight);

  RowInfo getActiveRowInfo();
  
  HasDataTable getGrid();
  
  Collection<RowInfo> getSelectedRows();  

  boolean isRowEditable(long rowId, boolean warn);
  
  boolean isRowSelected(long rowId);
  
  void refreshCellContent(long rowId, String columnSource);
  
  void updatePageSize(int pageSize, boolean init); 
}
