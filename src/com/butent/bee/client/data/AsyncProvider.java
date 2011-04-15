package com.butent.bee.client.data;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.CellColumn;
import com.butent.bee.client.grid.CellGrid;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.ViewInfo;
import com.butent.bee.shared.utils.BeeUtils;

public class AsyncProvider extends AbstractDataProvider<IsRow> {
  private class Callback implements Queries.RowSetCallback {
    private final HasData<IsRow> display;
    private final Range range;

    private Callback(HasData<IsRow> display, Range range) {
      this.display = display;
      this.range = range;
    }

    @Override
    public void onResponse(BeeRowSet rowSet) {
      if (!display.getVisibleRange().equals(range)) {
        BeeKeeper.getLog().warning("range changed");
        return;
      }

      if (rowSet.isEmpty()) {
        BeeKeeper.getLog().warning("rowset empty");
        return;
      }

      updateDisplay(rowSet);
    }

    private void updateDisplay(BeeRowSet data) {
      int start = range.getStart();
      int length = range.getLength();
      int rowCount = data.getNumberOfRows();

      BeeKeeper.getLog().info("upd", start, length, rowCount);

      Assert.nonNegative(start);
      Assert.isPositive(length);
      Assert.isPositive(rowCount);

      if (length == rowCount) {
        display.setRowData(start, data.getRows().getList());
      } else {
        display.setRowData(start,
            data.getRows().getList().subList(0, BeeUtils.min(length, rowCount)));
      }
    }
  }

  private ViewInfo viewInfo;
  private Filter filter = null;
  private Order order = null;

  public AsyncProvider(ViewInfo viewInfo) {
    super();
    this.viewInfo = viewInfo;
  }

  public Filter getFilter() {
    return filter;
  }

  public Order getOrder() {
    return order;
  }

  public ViewInfo getViewInfo() {
    return viewInfo;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public void setViewInfo(ViewInfo viewInfo) {
    this.viewInfo = viewInfo;
  }

  @Override
  protected void onRangeChanged(HasData<IsRow> display) {
    Assert.notNull(display);
    Range range = display.getVisibleRange();

    Filter flt = getFilter();

    Order ord = null;
    if (display instanceof CellGrid) {
      ord = getViewOrder((CellGrid) display);
    }
    if (ord == null) {
      ord = getOrder();
    }

    Queries.getRowSet(getViewInfo().getName(), flt, ord, range.getStart(), range.getLength(),
        new Callback(display, range));
  }

  private Order getViewOrder(CellGrid grid) {
    ColumnSortList sortList = grid.getColumnSortList();
    if (sortList == null) {
      return null;
    }

    Order ord = new Order();

    for (int i = 0; i < sortList.size(); i++) {
      ColumnSortInfo sortInfo = sortList.get(i);
      Column<?, ?> column = sortInfo.getColumn();
      if (!(column instanceof CellColumn)) {
        break;
      }
      String label = ((CellColumn<?>) column).getLabel();
      if (BeeUtils.isEmpty(label)) {
        break;
      }
      
      ord.add(label, sortInfo.isAscending());
    }
    return ord;
  }
}
