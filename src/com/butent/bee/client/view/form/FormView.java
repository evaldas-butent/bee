package com.butent.bee.client.view.form;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.add.HasAddEndHandlers;
import com.butent.bee.client.view.add.HasAddStartHandlers;
import com.butent.bee.client.view.add.HasReadyForInsertHandlers;
import com.butent.bee.client.view.edit.HasEditState;
import com.butent.bee.client.view.edit.HasReadyForUpdateHandlers;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;

import java.util.List;

/**
 * Contains necessary methods for form implementing classes.
 */

public interface FormView extends View, NotificationListener, HasEditState,
    HasAddStartHandlers, HasAddEndHandlers, HasReadyForInsertHandlers, HasReadyForUpdateHandlers {

  void applyOptions(String options);

  void create(FormDescription formDescription, List<BeeColumn> dataColumns, FormCallback callback);

  void finishNewRow(IsRow row);

  RowInfo getActiveRowInfo();

  List<BeeColumn> getDataColumns();

  int getDataIndex(String source);

  HasDataTable getDisplay();

  FormCallback getFormCallback();

  IsRow getRowData();

  JavaScriptObject getRowJso();

  Widget getWidgetBySource(String source);

  boolean isRowEditable(boolean warn);

  void prepareForInsert();

  void refreshCellContent(String columnSource);

  void showGrids(boolean show);

  void start(Integer rowCount);

  void startNewRow();

  void updateCell(String columnId, String newValue);

  void updateRowData(IsRow row);
}