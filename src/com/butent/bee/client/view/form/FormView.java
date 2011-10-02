package com.butent.bee.client.view.form;

import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.add.HasAddEndHandlers;
import com.butent.bee.client.view.add.HasAddStartHandlers;
import com.butent.bee.client.view.add.HasReadyForInsertHandlers;
import com.butent.bee.client.view.edit.HasReadyForUpdateHandlers;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;

import java.util.List;

/**
 * Contains necessary methods for form implementing classes.
 */

public interface FormView extends View, NotificationListener,
    HasAddStartHandlers, HasAddEndHandlers, HasReadyForInsertHandlers, HasReadyForUpdateHandlers {

  void applyOptions(String options);

  void create(FormDescription formDescription, List<BeeColumn> dataColumns);

  void finishNewRow(IsRow row);

  RowInfo getActiveRowInfo();

  HasDataTable getDisplay();

  IsRow getRowData();
  
  JavaScriptObject getRowJso();
  
  boolean isRowEditable(boolean warn);
  
  void prepareForInsert();

  void refreshCellContent(String columnSource);

  void start(int rowCount);

  void startNewRow();
  
  void updateCell(String columnId, String newValue);  
}