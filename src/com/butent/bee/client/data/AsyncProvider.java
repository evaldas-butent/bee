package com.butent.bee.client.data;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
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
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

public class AsyncProvider extends Provider {
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
      updateDisplay(range.getStart(), range.getLength(), rowSet);
    }
  }

  private DataInfo dataInfo;
  private Filter filter = null;
  private Order order = null;

  public AsyncProvider(HasData<IsRow> display, DataInfo dataInfo) {
    super(display);
    this.dataInfo = dataInfo;
  }

  public DataInfo getDataInfo() {
    return dataInfo;
  }

  public Filter getFilter() {
    return filter;
  }

  public Order getOrder() {
    return order;
  }

  public void setDataInfo(DataInfo dataInfo) {
    this.dataInfo = dataInfo;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public void updateDisplay(int start, int length, BeeRowSet data) {
    int rowCount = data.getNumberOfRows();

    BeeKeeper.getLog().info("upd", start, length, rowCount);

    Assert.nonNegative(start);
    Assert.isPositive(length);
    Assert.isPositive(rowCount);

    if (length == rowCount) {
      getDisplay().setRowData(start, data.getRows().getList());
    } else {
      getDisplay().setRowData(start,
          data.getRows().getList().subList(0, BeeUtils.min(length, rowCount)));
    }
  }

  @Override
  protected void onRangeChanged() {
    HasData<IsRow> displ = getDisplay();
    Range range = getRange();

    Filter flt = getFilter();

    Order ord = null;
    if (displ instanceof CellGrid) {
      ord = getViewOrder((CellGrid) displ);
    }
    if (ord == null) {
      ord = getOrder();
    }

    Queries.getRowSet(getDataInfo().getName(), flt, ord, range.getStart(), range.getLength(),
        new Callback(displ, range));
  }

  private Order getViewOrder(CellGrid grid) {
    ColumnSortList sortList = grid.getColumnSortList();
    if (sortList == null || sortList.size() <= 0) {
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
