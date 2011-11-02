package com.butent.bee.client.view.grid;

import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;

import java.util.List;

public class AbstractGridCallback implements GridCallback {

  public void afterCreate(CellGrid grid) {
  }

  public boolean afterCreateColumn(String columnId, AbstractColumn<?> column, ColumnHeader header,
      ColumnFooter footer) {
    return true;
  }

  public void afterCreateColumns(CellGrid grid) {
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

  public boolean onLoad(GridDescription gridDescription) {
    return true;
  }

  public void onShow(GridPresenter presenter) {
  }
}
