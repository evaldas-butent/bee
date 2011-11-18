package com.butent.bee.client.view.grid;

import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.add.HasAddEndHandlers;
import com.butent.bee.client.view.add.HasAddStartHandlers;
import com.butent.bee.client.view.add.HasReadyForInsertHandlers;
import com.butent.bee.client.view.edit.HasReadyForUpdateHandlers;
import com.butent.bee.client.view.edit.HasSaveChangesHandlers;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;
import java.util.List;

/**
 * Specifies necessary methods for grid view user interface component.
 */

public interface GridView extends DataView, HasAddStartHandlers, HasAddEndHandlers,
    HasReadyForInsertHandlers, HasReadyForUpdateHandlers, HasSaveChangesHandlers {

  void applyOptions(String options);

  void create(List<BeeColumn> dataColumns, int rowCount, BeeRowSet rowSet,
      GridDescription gridDescription, GridCallback gridCallback, boolean hasSearch);

  int estimatePageSize(int containerWidth, int containerHeight);
  
  IsRow getActiveRowData();

  CellGrid getGrid();
  
  GridCallback getGridCallback();

  Collection<RowInfo> getSelectedRows();

  boolean isRowEditable(long rowId, boolean warn);

  boolean isRowSelected(long rowId);

  void refreshCellContent(long rowId, String columnSource);

  void setRelColumn(String relColumn);
  
  void setRelId(long relId);
}
