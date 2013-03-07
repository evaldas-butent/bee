package com.butent.bee.client.view.grid;

import com.butent.bee.client.data.ParentRowCreator;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.ui.HandlesHistory;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.add.HasAddEndHandlers;
import com.butent.bee.client.view.add.HasAddStartHandlers;
import com.butent.bee.client.view.add.HasReadyForInsertHandlers;
import com.butent.bee.client.view.edit.HasEditFormHandlers;
import com.butent.bee.client.view.edit.HasReadyForUpdateHandlers;
import com.butent.bee.client.view.edit.HasSaveChangesHandlers;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;
import java.util.List;

/**
 * Specifies necessary methods for grid view user interface component.
 */

public interface GridView extends DataView, HasAddStartHandlers, HasAddEndHandlers,
    HasReadyForInsertHandlers, HasReadyForUpdateHandlers, HasSaveChangesHandlers,
    HasEditFormHandlers, ParentRowCreator, HandlesHistory, SearchView {
  
  public enum SelectedRows {
    ALL, EDITABLE
  }

  void applyOptions(String options);

  void create(List<BeeColumn> dataColumns, int rowCount, BeeRowSet rowSet,
      GridDescription gridDescription, GridInterceptor gridInterceptor, boolean hasSearch,
      Order order);

  int estimatePageSize(int containerWidth, int containerHeight);

  void formCancel();

  void formConfirm();

  List<BeeColumn> getDataColumns();

  FormView getForm(boolean edit);

  CellGrid getGrid();

  GridInterceptor getGridInterceptor();

  String getGridName();

  long getRelId();

  Collection<RowInfo> getSelectedRows(SelectedRows mode);

  boolean isAdding();

  boolean isReadOnly();

  boolean isRowEditable(IsRow row, boolean warn);

  boolean isRowSelected(long rowId);

  boolean likeAMotherlessChild();

  void refreshCellContent(long rowId, String columnSource);

  void setRelId(long relId);

  boolean validateFormData(FormView form, NotificationListener notificationListener,
      boolean focusOnError);
}
