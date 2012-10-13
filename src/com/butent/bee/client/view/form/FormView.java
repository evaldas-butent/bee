package com.butent.bee.client.view.form;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.event.logical.ActionEvent;
import com.butent.bee.client.event.logical.ActiveWidgetChangeEvent;
import com.butent.bee.shared.HasState;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.add.HasAddEndHandlers;
import com.butent.bee.client.view.add.HasAddStartHandlers;
import com.butent.bee.client.view.add.HasReadyForInsertHandlers;
import com.butent.bee.client.view.edit.HasReadyForUpdateHandlers;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.HasCaption;

import java.util.List;

/**
 * Contains necessary methods for form implementing classes.
 */

public interface FormView extends DataView, HasDataTable, ActiveWidgetChangeEvent.Handler,  
    HasAddStartHandlers, HasAddEndHandlers, HasReadyForInsertHandlers, HasReadyForUpdateHandlers,
    ActionEvent.HasActionHandlers, HasDimensions, HasState, HasCaption  {

  void applyOptions(String options);

  boolean checkForUpdate(boolean reset);
  
  void create(FormDescription formDescription, String viewName, List<BeeColumn> dataColumns,
      boolean addStyle, FormCallback callback);

  boolean focus(String source);
  
  RowInfo getActiveRowInfo();

  List<BeeColumn> getDataColumns();

  int getDataIndex(String source);

  HasDataTable getDisplay();

  FormCallback getFormCallback();
  
  String getFormName();

  Widget getRootWidget();
  
  JavaScriptObject getRowJso();

  Widget getWidgetBySource(String source);

  boolean isRowEditable(boolean warn);
  
  void onCancel(Command command);

  void prepareForInsert();

  void refreshCellContent(String columnSource);

  void refreshChildWidgets(IsRow row);
  
  void start(Integer rowCount);

  void updateCell(String columnId, String newValue);

  void updateRow(IsRow row, boolean refreshChildren);
  
  boolean validate();
}