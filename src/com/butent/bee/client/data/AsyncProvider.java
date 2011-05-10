package com.butent.bee.client.data;

import com.google.gwt.view.client.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.view.event.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;

/**
 * Extends {@code Provider} class and implements data range management from asynchronous data
 * transfers from the server side.
 */

public class AsyncProvider extends Provider {

  private class Callback implements Queries.RowSetCallback {
    private final HasDataTable display;
    private final Range range;

    private Callback(HasDataTable display, Range range) {
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

  public AsyncProvider(HasDataTable display, DataInfo dataInfo) {
    super(display);
    Assert.notNull(dataInfo);
    this.dataInfo = dataInfo;
  }

  public DataInfo getDataInfo() {
    return dataInfo;
  }

  @Override
  public void onSort(SortEvent event) {
    Assert.notNull(event);
    setOrder(event.getOrder());
    goTop();
  }

  public void updateDisplay(int start, int length, BeeRowSet data) {
    int rowCount = data.getNumberOfRows();

    if (Global.isDebug()) {
      BeeKeeper.getLog().info("upd", start, length, rowCount);
    }

    Assert.nonNegative(start);
    Assert.isPositive(length);
    Assert.isPositive(rowCount);

    if (length >= rowCount) {
      getDisplay().setRowData(start, data.getRows().getList());
    } else {
      getDisplay().setRowData(start, data.getRows().getList().subList(0, length));
    }
  }

  @Override
  protected void onRangeChanged() {
    HasDataTable displ = getDisplay();
    Range range = getRange();

    Filter flt = getFilter();
    Order ord = getOrder();

    Queries.getRowSet(getDataInfo().getName(), flt, ord, range.getStart(), range.getLength(),
        CachingPolicy.FULL, new Callback(displ, range));
  }
}
