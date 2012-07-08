package com.butent.bee.client.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

/**
 * Extends {@code Provider} class and implements data range management from asynchronous data
 * transfers from the server side.
 */

public class AsyncProvider extends Provider {

  private class Callback implements Queries.RowSetCallback {
    private final Range<Integer> queryRange;

    private Range<Integer> displayRange;
    private boolean updateActiveRow;

    private Integer rpcId = null;

    private Callback(Range<Integer> queryRange, Range<Integer> displayRange,
        boolean updateActiveRow) {
      this.queryRange = queryRange;
      this.displayRange = displayRange;
      this.updateActiveRow = updateActiveRow;
    }

    public void onSuccess(BeeRowSet rowSet) {
      Integer id = getRpcId();
      if (id != null) {
        if (pendingRequests.containsKey(id)) {
          pendingRequests.remove(id);
        } else {
          BeeKeeper.getLog().info("response", id, "ignored");
          return;
        }
      }

      if (getPageStart() != getDisplayOffset() || getPageSize() != getDisplayLimit()) {
        BeeKeeper.getLog().warning("range changed:", getDisplayOffset(), getDisplayLimit(),
            getPageStart(), getPageSize());
        return;
      }

      updateDisplay(rowSet, getQueryOffset(), updateActiveRow());
    }

    private int getDisplayLimit() {
      if (displayRange.hasUpperBound()) {
        return displayRange.upperEndpoint() - displayRange.lowerEndpoint();
      } else {
        return BeeConst.UNDEF;
      }
    }

    private int getDisplayOffset() {
      return displayRange.lowerEndpoint();
    }

    private int getQueryOffset() {
      return queryRange.lowerEndpoint();
    }

    private Range<Integer> getQueryRange() {
      return queryRange;
    }

    private Integer getRpcId() {
      return rpcId;
    }

    private void setDisplayRange(Range<Integer> displayRange) {
      this.displayRange = displayRange;
    }

    private void setRpcId(Integer rpcId) {
      this.rpcId = rpcId;
    }

    private void setUpdateActiveRow(boolean updateActiveRow) {
      this.updateActiveRow = updateActiveRow;
    }

    private boolean updateActiveRow() {
      return updateActiveRow;
    }
  }

  private final CachingPolicy cachingPolicy;

  private final Map<Integer, Callback> pendingRequests = Maps.newLinkedHashMap();

  private int lastOffset = BeeConst.UNDEF;

  private final boolean debug = false;

  public AsyncProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, String idColumnName, String versionColumnName,
      Filter immutableFilter, CachingPolicy cachingPolicy) {
    super(display, notificationListener, viewName, columns, idColumnName, versionColumnName,
        immutableFilter);
    this.cachingPolicy = cachingPolicy;
  }

  @Override
  public void clear() {
    resetRequests();
    super.clear();
  }

  public CachingPolicy getCachingPolicy() {
    return cachingPolicy;
  }

  public void onFilterChange(final Filter newFilter, final boolean updateActiveRow) {
    resetRequests();
    Filter flt = getQueryFilter(newFilter);

    Queries.getRowCount(getViewName(), flt, new Queries.IntCallback() {
      @Override
      public void onSuccess(Integer result) {
        if (newFilter == null || BeeUtils.isPositive(result)) {
          acceptFilter(newFilter);
          onRowCount(result, updateActiveRow);
        } else {
          rejectFilter(newFilter);
        }
      }
    });
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      getDisplay().onMultiDelete(event);

      int rowCount = Math.max(getRowCount() - event.getSize(), 0);
      getDisplay().setRowCount(rowCount, true);

      onRequest(false);
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      getDisplay().onRowDelete(event);

      int rowCount = Math.max(getRowCount() - 1, 0);
      getDisplay().setRowCount(rowCount, true);

      onRequest(false);
    }
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
  }

  @Override
  public void onSort(SortEvent event) {
    Assert.notNull(event);
    setOrder(event.getOrder());
    resetRequests();
    goTop();
  }

  @Override
  public void refresh(final boolean updateActiveRow) {
    resetRequests();
    Global.getCache().remove(getViewName());

    if (hasPaging()) {
      startLoading();
      Queries.getRowCount(getViewName(), getFilter(), new Queries.IntCallback() {
        @Override
        public void onSuccess(Integer result) {
          onRowCount(result, updateActiveRow);
        }
      });

    } else {
      onRequest(updateActiveRow);
    }
  }

  @Override
  protected void onRequest(boolean updateActiveRow) {
    int offset = getPageStart();
    int limit = getPageSize();

    Boolean forward = getDirection(offset);
    setLastOffset(offset);

    Range<Integer> displayRange = createRange(offset, limit);

    if (limit > 0 && !pendingRequests.isEmpty()) {
      Integer requestId = null;

      for (Map.Entry<Integer, Callback> entry : pendingRequests.entrySet()) {
        if (entry.getValue().getQueryRange().encloses(displayRange)) {
          entry.getValue().setDisplayRange(displayRange);
          entry.getValue().setUpdateActiveRow(updateActiveRow);

          if (pendingRequests.size() > 1) {
            requestId = entry.getKey();
            break;
          } else {
            if (debug) {
              BeeKeeper.getLog().debug("updated", entry.getKey(), offset, limit);
            }
            return;
          }
        }
      }

      if (requestId != null) {
        List<Integer> rpcIds = ImmutableList.copyOf(pendingRequests.keySet());
        for (Integer id : rpcIds) {
          if (!requestId.equals(id)) {
            BeeKeeper.getRpc().cancelRequest(id);
            pendingRequests.remove(id);
            if (debug) {
              BeeKeeper.getLog().debug("canceled", id);
            }
          }
        }
        return;
      }
    }

    cancelPendingRequests();

    Filter flt = getFilter();
    Order ord = getOrder();

    CachingPolicy caching = getCachingPolicy();
    if (caching != null && caching.doRead()) {
      BeeRowSet rowSet = Global.getCache().getRowSet(getViewName(), flt, ord, offset, limit);
      if (rowSet != null) {
        if (debug) {
          BeeKeeper.getLog().debug("cache", offset, limit);
        }
        updateDisplay(rowSet, offset, updateActiveRow);
        return;
      }
      caching = CachingPolicy.disableRead(getCachingPolicy());
    }

    int queryOffset = offset;
    int queryLimit = limit;

    if (limit > 0 && forward != null) {
      if (!forward && offset > 0) {
        int margin = Math.min(offset, limit);
        queryOffset -= margin;
        queryLimit += margin;
      } else if (forward && offset + limit < getRowCount()) {
        int margin = Math.min(getRowCount() - offset - limit, limit);
        queryLimit += margin;
      }
    }

    Range<Integer> queryRange = createRange(queryOffset, queryLimit);
    Callback callback = new Callback(queryRange, displayRange, updateActiveRow);

    int rpcId = Queries.getRowSet(getViewName(), null, flt, ord,
        queryOffset, queryLimit, caching, callback);

    if (!Queries.isResponseFromCache(rpcId)) {
      startLoading();
      callback.setRpcId(rpcId);
      pendingRequests.put(rpcId, callback);
      if (debug) {
        BeeKeeper.getLog().debug("pending", rpcId, offset, limit);
      }
    }
  }

  private void cancelPendingRequests() {
    if (!pendingRequests.isEmpty()) {
      for (int rpcId : pendingRequests.keySet()) {
        BeeKeeper.getRpc().cancelRequest(rpcId);
      }
      pendingRequests.clear();
    }
  }

  private Range<Integer> createRange(int offset, int limit) {
    if (limit > 0) {
      return Ranges.closedOpen(offset, offset + limit);
    } else {
      return Ranges.atLeast(offset);
    }
  }

  private Boolean getDirection(int offset) {
    if (BeeConst.isUndef(getLastOffset()) || offset == getLastOffset()) {
      return null;
    } else {
      return offset > getLastOffset();
    }
  }

  private int getLastOffset() {
    return lastOffset;
  }

  private int getRowCount() {
    return getDisplay().getRowCount();
  }
  
  private void onRowCount(Integer rowCount, boolean updateActiveRow) {
    if (BeeUtils.isPositive(rowCount)) {
      getDisplay().setRowCount(rowCount, true);
      onRequest(updateActiveRow);
    } else {
      getDisplay().setRowCount(0, true);
      getDisplay().setRowData(null, true);
    }
  }

  private void resetRequests() {
    cancelPendingRequests();
    setLastOffset(BeeConst.UNDEF);
  }

  private void setLastOffset(int lastOffset) {
    this.lastOffset = lastOffset;
  }

  private void updateDisplay(BeeRowSet data, int queryOffset, boolean updateActiveRow) {
    int rowCount = data.getNumberOfRows();

    int displayOffset = getPageStart();
    int displayLimit = getPageSize();

    List<BeeRow> rows;
    if (rowCount <= 0) {
      rows = Lists.newArrayList();

    } else if (displayLimit <= 0) {
      rows = data.getRows().getList();

    } else if (queryOffset >= displayOffset) {
      if (displayLimit < rowCount) {
        rows = data.getRows().getList().subList(0, displayLimit);
      } else {
        rows = data.getRows().getList();
      }

    } else {
      int fromIndex = displayOffset - queryOffset;
      if (fromIndex < rowCount) {
        int toIndex = Math.min(rowCount, fromIndex + displayLimit);
        rows = data.getRows().getList().subList(fromIndex, toIndex);
      } else {
        rows = Lists.newArrayList();
      }
    }

    if (debug) {
      BeeKeeper.getLog().debug(queryOffset, displayOffset, displayLimit, rowCount, rows.size());
    }

    if (!hasPaging()) {
      getDisplay().setRowCount(rowCount, true);
    }
    if (updateActiveRow) {
      getDisplay().updateActiveRow(rows);
    }
    getDisplay().setRowData(rows, true);
  }
}
