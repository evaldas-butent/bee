package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;

import java.util.List;

/**
 * Extends {@code Provider} class and implements data range management from asynchronous data
 * transfers from the server side.
 */

public class AsyncProvider extends Provider {

  private class Callback implements Queries.RowSetCallback {
    private final Range range;
    private final boolean updateActiveRow;
    private Integer rpcId = null;

    private Callback(Range range, boolean updateActiveRow) {
      this.range = range;
      this.updateActiveRow = updateActiveRow;
    }

    public void onFailure(String[] reason) {
    }

    public void onSuccess(BeeRowSet rowSet) {
      Integer id = getRpcId();
      if (id != null) {
        if (getPendingRequests().contains(id)) {
          getPendingRequests().remove(id);
          BeeKeeper.getLog().info("response", id, "range", range.getStart(), range.getLength());
        } else {
          BeeKeeper.getLog().info("response", id, "ignored");
          return;
        }
      }

      if (!getDisplay().getVisibleRange().equals(range)) {
        BeeKeeper.getLog().warning("range changed");
        return;
      }

      updateDisplay(range.getStart(), range.getLength(), rowSet, updateActiveRow);
    }

    private Integer getRpcId() {
      return rpcId;
    }

    private void setRpcId(Integer rpcId) {
      this.rpcId = rpcId;
    }
  }

  private CachingPolicy cachingPolicy = CachingPolicy.FULL;
  
  private final List<Integer> pendingRequests = Lists.newArrayList();

  public AsyncProvider(HasDataTable display, String viewName, List<BeeColumn> columns,
      String idColumnName, String versionColumnName, Filter dataFilter) {
    super(display, viewName, columns, idColumnName, versionColumnName, dataFilter);
  }

  public CachingPolicy getCachingPolicy() {
    return cachingPolicy;
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
  }

  @Override
  public void onSort(SortEvent event) {
    Assert.notNull(event);
    setOrder(event.getOrder());
    goTop();
  }
  
  public void setCachingPolicy(CachingPolicy cachingPolicy) {
    this.cachingPolicy = cachingPolicy;
  }

  public void updateDisplay(int start, int length, BeeRowSet data, boolean updateActiveRow) {
    Assert.nonNegative(start);
    int rowCount = data.getNumberOfRows();
    
    List<? extends IsRow> rowValues; 
    if (length <= 0 || rowCount <= 0) {
      rowValues = Lists.newArrayList();
    } else if (length >= rowCount) {
      rowValues = data.getRows().getList();
    } else {
      rowValues = data.getRows().getList().subList(0, length);
    }

    if (updateActiveRow) {
      getDisplay().updateActiveRow(rowValues);
    }
    getDisplay().setRowData(start, rowValues);
  }

  @Override
  protected void onRangeChanged(boolean updateActiveRow) {
    cancelPendingRequests();
    startLoading();
    
    Range range = getRange();

    Filter flt = getFilter();
    Order ord = getOrder();
    
    CachingPolicy caching = isCacheEnabled() ? getCachingPolicy() : CachingPolicy.NONE;
    Callback callback = new Callback(range, updateActiveRow);    

    int rpcId = Queries.getRowSet(getViewName(), null, flt, ord,
        range.getStart(), range.getLength(), caching, callback);

    if (!Queries.isResponseFromCache(rpcId)) {
      callback.setRpcId(rpcId);
      getPendingRequests().add(rpcId);
    }
  }

  @Override
  protected void onRefresh() {
    onRangeChanged(true);
  }
  
  private void cancelPendingRequests() {
    if (!getPendingRequests().isEmpty()) {
      for (int rpcId : getPendingRequests()) {
        BeeKeeper.getRpc().cancelRequest(rpcId);
      }
      getPendingRequests().clear();
    }
  }

  private List<Integer> getPendingRequests() {
    return pendingRequests;
  }
}
