package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.view.event.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CachedProvider extends Provider {
  private IsTable<?, ?> table;

  public CachedProvider(HasDataTable display, IsTable<?, ?> table) {
    super(display);
    Assert.notNull(table);
    this.table = table;
  }

  public int getRowCount() {
    return table.getNumberOfRows();
  }
  
  public IsTable<?, ?> getTable() {
    return table;
  }
  
  public void onSort(SortEvent event) {
    if (getRowCount() <= 1) {
      return;
    }
    Order order = event.getOrder();

    if (order == null || order.getSize() <= 0) {
      getTable().sortByRowId(true);
    } else {
      List<Pair<Integer, Boolean>> sortList = Lists.newArrayList();

      for (Order.Column sortInfo : order.getColumns()) {
        int index = getTable().getColumnIndex(sortInfo.getLabel());
        if (index < 0 || index > getTable().getNumberOfColumns()) {
          BeeKeeper.getLog().warning("onSort: column id", sortInfo.getLabel(), "not found");
        }
        sortList.add(new Pair<Integer, Boolean>(index, sortInfo.isAscending()));
      }
      if (!sortList.isEmpty()) {
        getTable().sort(sortList);
      }
    }
    goTop(true);
  }
 
  public void refreshDisplay() {
    Range range = getRange();
    int start = range.getStart();
    int length = range.getLength();
    int rowCount = getRowCount();
    
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
