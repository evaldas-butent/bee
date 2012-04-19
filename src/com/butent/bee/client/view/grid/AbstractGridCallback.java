package com.butent.bee.client.view.grid;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AbstractGridCallback implements GridCallback {

  private GridPresenter gridPresenter = null;

  public void afterAction(Action action, GridPresenter presenter) {
  }

  public void afterCreate(CellGrid grid) {
  }

  public boolean afterCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer) {
    return true;
  }

  public void afterCreateColumns(CellGrid grid) {
  }

  public void afterCreateWidget(String name, Widget widget) {
  }

  public void afterDeleteRow(long rowId) {
  }

  public boolean beforeAction(Action action, GridPresenter presenter) {
    return true;
  }

  public boolean beforeAddRow(GridPresenter presenter) {
    return true;
  }

  public void beforeCreate(List<? extends IsColumn> dataColumns, int rowCount,
      GridDescription gridDescription, boolean hasSearch) {
  }

  public boolean beforeCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {
    return true;
  }

  public void beforeCreateColumns(List<? extends IsColumn> dataColumns,
      List<ColumnDescription> columnDescriptions) {
  }

  public boolean beforeCreateWidget(String name, Element description) {
    return true;
  }

  public int beforeDeleteRow(GridPresenter presenter, IsRow row) {
    return 0;
  }

  public int beforeDeleteRows(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows) {
    return 0;
  }

  public void beforeRefresh(GridPresenter presenter) {
  }

  public void beforeRequery(GridPresenter presenter) {
  }

  public Widget createCustomWidget(String name, Element description) {
    return null;
  }

  public String getCaption() {
    return null;
  }

  public GridPresenter getGridPresenter() {
    return gridPresenter;
  }

  public Map<String, Filter> getInitialFilters() {
    return null;
  }

  public BeeRowSet getInitialRowSet() {
    return null;
  }

  public GridCallback getInstance() {
    return null;
  }

  public String getRowCaption(IsRow row, boolean edit) {
    return null;
  }

  public boolean onClose(GridPresenter presenter) {
    return true;
  }

  public boolean onLoad(GridDescription gridDescription) {
    return true;
  }

  public boolean onLoadExtWidget(Element root) {
    return true;
  }

  @Override
  public boolean onPrepareForInsert(GridView gridView, IsRow newRow,
      List<? extends IsColumn> columns) {
    return true;
  }

  @Override
  public boolean onPrepareForUpdate(GridView gridView, IsRow oldRow,
      List<? extends IsColumn> columns, List<String> newValues) {
    return true;
  }

  public void onShow(GridPresenter presenter) {
  }

  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    return true;
  }

  public void setGridPresenter(GridPresenter gridPresenter) {
    this.gridPresenter = gridPresenter;
  }
}
