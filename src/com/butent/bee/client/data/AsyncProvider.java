package com.butent.bee.client.data;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Extends {@code Provider} class and implements data range management from asynchronous data
 * transfers from the server side.
 */

public class AsyncProvider extends Provider {

  private class Callback implements Queries.RowSetCallback {
    private final int offset;
    private final int limit;
    private final boolean updateActiveRow;

    private Integer rpcId = null;

    private Callback(int offset, int limit, boolean updateActiveRow) {
      this.offset = offset;
      this.limit = limit;
      this.updateActiveRow = updateActiveRow;
    }

    public void onSuccess(BeeRowSet rowSet) {
      Integer id = getRpcId();
      if (id != null) {
        if (getPendingRequests().contains(id)) {
          getPendingRequests().remove(id);
          BeeKeeper.getLog().info("response", id, "range", getOffset(), getLimit());
        } else {
          BeeKeeper.getLog().info("response", id, "ignored");
          return;
        }
      }

      if (getPageStart() != getOffset() || getPageSize() != getLimit()) {
        BeeKeeper.getLog().warning("range changed");
        return;
      }

      updateDisplay(rowSet, getLimit(), updateActiveRow());
    }

    private int getLimit() {
      return limit;
    }

    private int getOffset() {
      return offset;
    }

    private Integer getRpcId() {
      return rpcId;
    }

    private void setRpcId(Integer rpcId) {
      this.rpcId = rpcId;
    }

    private boolean updateActiveRow() {
      return updateActiveRow;
    }
  }

  private CachingPolicy cachingPolicy = CachingPolicy.FULL;

  private final List<Integer> pendingRequests = Lists.newArrayList();

  public AsyncProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns) {
    this(display, notificationListener, viewName, columns, null, null, null);
  }

  public AsyncProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, Filter immutableFilter) {
    this(display, notificationListener, viewName, columns, null, null, immutableFilter);
  }

  public AsyncProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, String idColumnName, String versionColumnName,
      Filter immutableFilter) {
    super(display, notificationListener, viewName, columns, idColumnName, versionColumnName,
        immutableFilter);
  }

  @Override
  public void clear() {
    cancelPendingRequests();
    super.clear();
  }

  public CachingPolicy getCachingPolicy() {
    return cachingPolicy;
  }

  public void onFilterChange(final Filter newFilter) {
    if (newFilter == null) {
      acceptFilter(newFilter);
      refresh(true);

    } else {
      Filter flt = getQueryFilter(newFilter);
      Queries.getRowCount(getViewName(), flt, new Queries.IntCallback() {
        @Override
        public void onSuccess(Integer result) {
          if (BeeUtils.isPositive(result)) {
            acceptFilter(newFilter);
            getDisplay().setRowCount(result, true);
            onRequest(true);
          } else {
            rejectFilter(newFilter);
          }
        }
      });
    }
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

  @Override
  public void requery(boolean updateActiveRow) {
    onRequest(updateActiveRow);
  }

  public void setCachingPolicy(CachingPolicy cachingPolicy) {
    this.cachingPolicy = cachingPolicy;
  }

  @Override
  public void refresh(final boolean updateActiveRow) {
    Global.getCache().removeQuietly(getViewName());

    if (hasPaging()) {
      startLoading();
      Queries.getRowCount(getViewName(), getFilter(), new Queries.IntCallback() {
        @Override
        public void onSuccess(Integer result) {
          getDisplay().setRowCount(result, true);
          onRequest(updateActiveRow);
        }
      });

    } else {
      onRequest(updateActiveRow);
    }
  }

  @Override
  protected void onDelete() {
    if (getDisplay().getRowCount() > 0) {
      refresh(false);
    }
  }

  @Override
  protected void onRequest(boolean updateActiveRow) {
    cancelPendingRequests();
    startLoading();

    int offset = getPageStart();
    int limit = getPageSize();

    Filter flt = getFilter();
    Order ord = getOrder();

    CachingPolicy caching = isCacheEnabled() ? getCachingPolicy() : CachingPolicy.NONE;
    Callback callback = new Callback(offset, limit, updateActiveRow);

    int rpcId = Queries.getRowSet(getViewName(), null, flt, ord,
        offset, limit, caching, callback);

    if (!Queries.isResponseFromCache(rpcId)) {
      callback.setRpcId(rpcId);
      getPendingRequests().add(rpcId);
    }
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

  private void updateDisplay(BeeRowSet data, int length, boolean updateActiveRow) {
    int rowCount = data.getNumberOfRows();

    List<? extends IsRow> rowValues;
    if (rowCount <= 0) {
      rowValues = Lists.newArrayList();
    } else if (length <= 0 || length >= rowCount) {
      rowValues = data.getRows().getList();
    } else {
      rowValues = data.getRows().getList().subList(0, length);
    }

    if (!hasPaging()) {
      getDisplay().setRowCount(rowCount, true);
    }
    if (updateActiveRow) {
      getDisplay().updateActiveRow(rowValues);
    }
    getDisplay().setRowData(rowValues, true);
  }
}
