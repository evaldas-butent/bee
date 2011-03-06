package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;

import com.butent.bee.client.data.DataProvider;
import com.butent.bee.shared.data.sort.SortInfo;
import com.butent.bee.shared.data.sort.SortOrder;

public class TableSorter implements ColumnSortEvent.Handler {
  private DataProvider provider;

  public TableSorter(DataProvider provider) {
    this.provider = provider;
  }

  public void onColumnSort(ColumnSortEvent event) {
    Column<?, ?> column = event.getColumn();
    if (column instanceof CellColumn) {
      int index = ((CellColumn<?>) column).getIndex();
      if (event.isSortAscending()) {
        provider.getTable().sort(index);
      } else {
        provider.getTable().sort(new SortInfo(index, SortOrder.DESCENDING));
      }
      provider.refreshDisplays();
    }
  }
}
