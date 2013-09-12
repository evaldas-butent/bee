package com.butent.bee.client.view.grid;

import com.google.gwt.xml.client.Element;

import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.ProvidesGridColumnRenderer;
import com.butent.bee.client.ui.WidgetInterceptor;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterDescription;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.HasCaption;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GridInterceptor extends WidgetInterceptor, ParentRowEvent.Handler, HasCaption,
    EditStartEvent.Handler, ProvidesGridColumnRenderer {

  public enum DeleteMode {
    CANCEL, DEFAULT, SILENT, CONFIRM, SINGLE, MULTI;
  }

  void afterAction(Action action, GridPresenter presenter);

  void afterCreate(GridView gridView);

  boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn);

  void afterCreateColumns(GridView gridView);

  void afterDeleteRow(long rowId);

  void afterInsertRow(IsRow result);

  void afterUpdateCell(IsColumn column, IsRow result, boolean rowMode);

  void afterUpdateRow(IsRow result);

  boolean beforeAction(Action action, GridPresenter presenter);

  boolean beforeAddRow(GridPresenter presenter);

  void beforeCreate(List<? extends IsColumn> dataColumns, GridDescription gridDescription);

  ColumnDescription beforeCreateColumn(GridView gridView, ColumnDescription columnDescription);

  void beforeCreateColumns(List<? extends IsColumn> dataColumns,
      List<ColumnDescription> columnDescriptions);

  DeleteMode beforeDeleteRow(GridPresenter presenter, IsRow row);

  DeleteMode beforeDeleteRows(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows);

  void beforeRefresh(GridPresenter presenter);

  String getColumnCaption(String columnName);

  DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode);

  List<String> getDeleteRowMessage(IsRow row);

  Pair<String, String> getDeleteRowsMessage(int selectedRows);

  AbstractFilterSupplier getFilterSupplier(String columnName, ColumnDescription columnDescription);

  ColumnFooter getFooter(String columnName, ColumnDescription columnDescription);

  GridPresenter getGridPresenter();

  ColumnHeader getHeader(String columnName, String caption);

  Map<String, Filter> getInitialParentFilters();

  BeeRowSet getInitialRowSet(GridDescription gridDescription);

  GridInterceptor getInstance();
  
  List<FilterDescription> getPredefinedFilters(List<FilterDescription> defaultFilters);

  String getRowCaption(IsRow row, boolean edit);

  void onAttach(GridView gridView);
  
  boolean onClose(GridPresenter presenter);

  boolean onLoad(GridDescription gridDescription);

  boolean onLoadExtWidget(Element root);

  void onReadyForInsert(GridView gridView, ReadyForInsertEvent event);

  void onReadyForUpdate(GridView gridView, ReadyForUpdateEvent event);

  void onSaveChanges(GridView gridView, SaveChangesEvent event);

  void onShow(GridPresenter presenter);

  boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow);

  void setGridPresenter(GridPresenter gridPresenter);
}
