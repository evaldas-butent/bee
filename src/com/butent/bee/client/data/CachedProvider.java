package com.butent.bee.client.data;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.grid.CellColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.sort.SortInfo;
import com.butent.bee.shared.data.sort.SortOrder;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CachedProvider extends Provider implements ColumnSortEvent.Handler {
  private IsTable<?, ?> table;

  public CachedProvider(HasData<IsRow> display, IsTable<?, ?> table) {
    super(display);
    this.table = table;
  }

  public IsTable<?, ?> getTable() {
    return table;
  }
  
  public void onColumnSort(ColumnSortEvent event) {
    Column<?, ?> column = event.getColumn();
    if (column instanceof CellColumn) {
      int index = ((CellColumn<?>) column).getIndex();
      if (event.isSortAscending()) {
        getTable().sort(index);
      } else {
        getTable().sort(new SortInfo(index, SortOrder.DESCENDING));
      }
      refreshDisplay();
    }
  }
 
  public void refreshDisplay() {
    Range range = getRange();
    int start = range.getStart();
    int length = range.getLength();
    int rowCount = table.getNumberOfRows();
    
    if (start == 0 && length == rowCount) {
      getDisplay().setRowData(start, getRowList());
    } else if (start >= 0 && start < rowCount && length > 0) {
      getDisplay().setRowData(start, getRowList().subList(start, 
          BeeUtils.min(start + length, rowCount)));
    }
  }

  @Override
  protected void onRangeChanged() {
    refreshDisplay();
  }

  private List<? extends IsRow> getRowList() {
    return table.getRows().getList();
  }
}
