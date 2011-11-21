package com.butent.bee.client.view.grid;

import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;
import java.util.List;

public interface GridCallback {

  void afterCreate(CellGrid grid);

  boolean afterCreateColumn(String columnId, AbstractColumn<?> column, ColumnHeader header,
      ColumnFooter footer);

  void afterCreateColumns(CellGrid grid);
  
  void beforeCreate(List<BeeColumn> dataColumns, int rowCount, GridDescription gridDescription,
      boolean hasSearch);
  
  boolean beforeCreateColumn(String columnId, List<BeeColumn> dataColumns,
      ColumnDescription columnDescription);

  void beforeCreateColumns(List<BeeColumn> dataColumns, List<ColumnDescription> columnDescriptions);
  
  int beforeDeleteRow(GridPresenter presenter, IsRow row);

  int beforeDeleteRows(GridPresenter presenter, IsRow activeRow, Collection<RowInfo> selectedRows);

  void beforeRefresh();

  void beforeRequery();
  
  GridCallback getInstance();
  
  boolean onLoad(GridDescription gridDescription);
  
  void onShow(GridPresenter presenter);
}
