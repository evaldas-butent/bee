package com.butent.bee.client.view.grid;

import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AbstractGridCallback implements GridCallback {

  public void afterCreate(CellGrid grid) {
  }

  public boolean afterCreateColumn(String columnId, AbstractColumn<?> column, ColumnHeader header,
      ColumnFooter footer) {
    return true;
  }

  public void afterCreateColumns(CellGrid grid) {
  }

  public boolean beforeAddRow(GridPresenter presenter) {
    return true;
  }

  public void beforeCreate(List<BeeColumn> dataColumns, int rowCount,
      GridDescription gridDescription, boolean hasSearch) {
  }

  public boolean beforeCreateColumn(String columnId, List<BeeColumn> dataColumns,
      ColumnDescription columnDescription) {
    return true;
  }

  public void beforeCreateColumns(List<BeeColumn> dataColumns,
      List<ColumnDescription> columnDescriptions) {
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

  public Map<String, Filter> getInitialFilters() {
    return null;
  }

  public GridCallback getInstance() {
    return null;
  }

  public boolean onClose(GridPresenter presenter) {
    return true;
  }

  public boolean onLoad(GridDescription gridDescription) {
    return true;
  }

  public void onShow(GridPresenter presenter) {
  }

  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    return true;
  }
}
