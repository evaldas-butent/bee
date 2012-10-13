package com.butent.bee.client.view.grid;

import com.google.gwt.xml.client.Element;

import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.ui.WidgetCallback;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.HasCaption;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GridCallback extends WidgetCallback, ParentRowEvent.Handler, HasCaption {

  int DELETE_CANCEL = -1;
  int DELETE_DEFAULT = 0;
  int DELETE_SILENT = 1;
  int DELETE_CONFIRM = 2;

  void afterAction(Action action, GridPresenter presenter);

  void afterCreate(GridView gridView);

  boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn);

  void afterCreateColumns(GridView gridView);

  void afterDeleteRow(long rowId);

  boolean beforeAction(Action action, GridPresenter presenter);

  boolean beforeAddRow(GridPresenter presenter);

  void beforeCreate(List<? extends IsColumn> dataColumns, int rowCount,
      GridDescription gridDescription, boolean hasSearch);

  boolean beforeCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription);

  void beforeCreateColumns(List<? extends IsColumn> dataColumns,
      List<ColumnDescription> columnDescriptions);

  int beforeDeleteRow(GridPresenter presenter, IsRow row);

  int beforeDeleteRows(GridPresenter presenter, IsRow activeRow, Collection<RowInfo> selectedRows);

  void beforeRefresh(GridPresenter presenter);

  String getDeleteRowMessage();

  Pair<String, String> getDeleteRowsMessage(int selectedRows);

  GridPresenter getGridPresenter();

  Map<String, Filter> getInitialFilters();

  BeeRowSet getInitialRowSet();

  GridCallback getInstance();

  AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription);

  String getRowCaption(IsRow row, boolean edit);

  boolean onClose(GridPresenter presenter);

  boolean onLoad(GridDescription gridDescription);

  boolean onLoadExtWidget(Element root);

  boolean onPrepareForInsert(GridView gridView, List<? extends IsColumn> columns,
      List<String> values);

  boolean onPrepareForUpdate(GridView gridView, long rowId, long version,
      List<? extends IsColumn> columns, List<String> oldValues, List<String> newValues);

  void onShow(GridPresenter presenter);

  boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow);

  void setGridPresenter(GridPresenter gridPresenter);
}
