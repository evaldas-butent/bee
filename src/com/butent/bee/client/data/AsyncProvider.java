package com.butent.bee.client.data;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.view.event.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
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

  private final DataInfo dataInfo;
  private Filter filter = null;
  private Order order = null;

  public AsyncProvider(HasDataTable display, DataInfo dataInfo) {
    super(display);
    Assert.notNull(dataInfo);
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

  @Override
  public void onSort(SortEvent event) {
    Assert.notNull(event);
    setOrder(event.getOrder());
    
    goTop(true);
  }
  
  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public void updateDisplay(int start, int length, BeeRowSet data) {
    int rowCount = data.getNumberOfRows();
    
    if (Global.isDebug()) {
      BeeKeeper.getLog().info("upd", start, length, rowCount);
    }

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
    Order ord = getOrder();

    Queries.getRowSet(getDataInfo().getName(), flt, ord, range.getStart(), range.getLength(),
        CachingPolicy.FULL, new Callback(displ, range));
  }
}
