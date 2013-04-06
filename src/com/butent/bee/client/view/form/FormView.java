package com.butent.bee.client.view.form;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.event.logical.ActionEvent;
import com.butent.bee.client.event.logical.ActiveWidgetChangeEvent;
import com.butent.bee.shared.HasState;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.add.HasAddEndHandlers;
import com.butent.bee.client.view.add.HasAddStartHandlers;
import com.butent.bee.client.view.add.HasReadyForInsertHandlers;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.HasReadyForUpdateHandlers;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.HasCaption;

import java.util.List;

/**
 * Contains necessary methods for form implementing classes.
 */

public interface FormView extends DataView, HasDataTable, ActiveWidgetChangeEvent.Handler,
    HasAddStartHandlers, HasAddEndHandlers, HasReadyForInsertHandlers, HasReadyForUpdateHandlers,
    ActionEvent.HasActionHandlers, HasDimensions, HasState, HasCaption, HasAllDragAndDropHandlers,
    EditEndEvent.Handler, SaveChangesEvent.Handler, RequiresResize, Printable {

  void applyOptions(String options);

  boolean checkOnClose();

  boolean checkOnSave();

  void create(FormDescription formDescription, String viewName, List<BeeColumn> dataColumns,
      boolean addStyle, FormInterceptor interceptor);

  void editRow(IsRow rowValue, Scheduler.ScheduledCommand focusCommand);
  
  boolean focus(String source);

  List<BeeColumn> getDataColumns();

  int getDataIndex(String source);

  HasDataTable getDisplay();

  FormInterceptor getFormInterceptor();

  String getFormName();

  IsRow getOldRow();

  IdentifiableWidget getRootWidget();

  JavaScriptObject getRowJso();

  Widget getWidgetByName(String name);

  Widget getWidgetBySource(String source);

  boolean isRowEditable(boolean warn);

  void onClose(CloseCallback closeCallback);

  void prepareForInsert();
  
  boolean printFooter();

  boolean printHeader();

  int refreshBySource(String source);

  void refreshChildWidgets(IsRow row);

  void start(Integer rowCount);

  void updateCell(String columnId, String newValue);

  void updateRow(IsRow row, boolean refreshChildren);

  boolean validate(NotificationListener notificationListener, boolean focusOnError);
}