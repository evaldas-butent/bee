package com.butent.bee.client.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.gwt.user.client.Timer;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.event.logical.DataRequestEvent;
import com.butent.bee.client.event.logical.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

/**
 * Extends {@code Provider} class and implements data range management from asynchronous data
 * transfers from the server side.
 */

public class AsyncProvider extends Provider {

  private static final BeeLogger logger = LogUtils.getLogger(AsyncProvider.class);
  
  private class QueryCallback extends Queries.RowSetCallback {

    private final Range<Integer> queryRange;

    private Range<Integer> displayRange;
    private boolean updateActiveRow;

    private Integer rpcId = null;
    private long startTime;

    private QueryCallback(Range<Integer> queryRange, Range<Integer> displayRange,
        boolean updateActiveRow) {
      this.queryRange = queryRange;
      this.displayRange = displayRange;
      this.updateActiveRow = updateActiveRow;
    }

    @Override
    public void onSuccess(BeeRowSet rowSet) {
      Integer id = getRpcId();
      if (id != null) {
        onResponse(getStartTime());
        if (pendingRequests.containsKey(id)) {
          pendingRequests.remove(id);
        } else {
          logger.info("response", id, "ignored");
          return;
        }
      }

      if (getPageStart() != getDisplayOffset() || getPageSize() != getDisplayLimit()) {
        logger.warning("range changed:", getDisplayOffset(), getDisplayLimit(),
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

    private long getStartTime() {
      return startTime;
    }

    private void setDisplayRange(Range<Integer> displayRange) {
      this.displayRange = displayRange;
    }

    private void setRpcId(Integer rpcId) {
      this.rpcId = rpcId;
    }

    private void setStartTime(long startTime) {
      this.startTime = startTime;
    }

    private void setUpdateActiveRow(boolean updateActiveRow) {
      this.updateActiveRow = updateActiveRow;
    }

    private boolean updateActiveRow() {
      return updateActiveRow;
    }
  }

  private class RequestScheduler extends Timer {
    private Filter queryFilter;
    private Order queryOrder;

    private int queryOffset;
    private int queryLimit;

    private CachingPolicy caching;

    private QueryCallback callback;

    private boolean active = false;
    private long lastTime = 0;

    @Override
    public void cancel() {
      if (isActive()) {
        super.cancel();
        setActive(false);
      }
    }

    @Override
    public void run() {
      int rpcId = Queries.getRowSet(getViewName(), null, queryFilter, queryOrder,
          queryOffset, queryLimit, caching, callback);

      if (!Queries.isResponseFromCache(rpcId)) {
        callback.setStartTime(System.currentTimeMillis());
        callback.setRpcId(rpcId);

        pendingRequests.put(rpcId, callback);
      }
    }

    private long getLastTime() {
      return lastTime;
    }

    private boolean isActive() {
      return active;
    }

    private void scheduleQuery(Filter flt, Order ord, int offset, int limit, CachingPolicy cp,
        QueryCallback cb) {
      cancel();

      this.queryFilter = flt;
      this.queryOrder = ord;
      this.queryOffset = offset;
      this.queryLimit = limit;
      this.caching = cp;
      this.callback = cb;

      long now = System.currentTimeMillis();
      long last = getLastTime();
      setLastTime(now);

      if (now - last < AsyncProvider.sensitivityMillis) {
        schedule(AsyncProvider.sensitivityMillis);
        setActive(true);
      } else {
        run();
      }
    }

    private void setActive(boolean active) {
      this.active = active;
    }

    private void setLastTime(long lastTime) {
      this.lastTime = lastTime;
    }
  }

  private static final int DEFAULT_SENSITIVITY_MILLIS = 300;
  private static final int DEFAULT_MAX_REPEAT_MILLIS = 100;

  private static final int DEFAULT_RPC_DURATION = 500;
  private static final int PREFETCH_SIZE_COEFFICIENT = 2;

  private static final int DEFAULT_MIN_PREFETCH_STEPS = 1;
  private static final int DEFAULT_MAX_PREFETCH_STEPS = 100;

  private static int sensitivityMillis = 0;
  private static int maxRepeatMillis = 0;

  private static int minPrefetchSteps = 0;
  private static int maxPrefetchSteps = 0;

  private final CachingPolicy cachingPolicy;
  private final boolean enablePrefetch;

  private final Map<Integer, QueryCallback> pendingRequests = Maps.newLinkedHashMap();
  private final RequestScheduler requestScheduler = new RequestScheduler();

  private int lastOffset = 0;

  private int rpcCount = 0;
  private long rpcMillis = 0;

  private int repeatStep = 0;
  private long lastRepeatTime = 0;

  private boolean prefetchPending = false;

  public AsyncProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, String idColumnName, String versionColumnName,
      Filter immutableFilter, CachingPolicy cachingPolicy) {

    super(display, notificationListener, viewName, columns, idColumnName, versionColumnName,
        immutableFilter);

    this.cachingPolicy = cachingPolicy;
    this.enablePrefetch = CachingPolicy.FULL.equals(cachingPolicy);

    if (AsyncProvider.sensitivityMillis <= 0) {
      AsyncProvider.sensitivityMillis = BeeUtils.positive(Settings.getProviderSensitivityMillis(),
          AsyncProvider.DEFAULT_SENSITIVITY_MILLIS);
      AsyncProvider.maxRepeatMillis = BeeUtils.positive(Settings.getProviderRepeatMillis(),
          AsyncProvider.DEFAULT_MAX_REPEAT_MILLIS);

      AsyncProvider.minPrefetchSteps = BeeUtils.positive(Settings.getProviderMinPrefetchSteps(),
          AsyncProvider.DEFAULT_MIN_PREFETCH_STEPS);
      AsyncProvider.maxPrefetchSteps = BeeUtils.positive(Settings.getProviderMaxPrefetchSteps(),
          AsyncProvider.DEFAULT_MAX_PREFETCH_STEPS);
    }
  }

  @Override
  public void clear() {
    resetRequests();
    super.clear();
  }

  public CachingPolicy getCachingPolicy() {
    return cachingPolicy;
  }

  @Override
  public void onDataRequest(DataRequestEvent event) {
    switch (event.getOrigin()) {
      case SCROLLER:
        requestScheduler.setLastTime(System.currentTimeMillis());
        resetRepeat();
        break;

      case KEYBOARD:
      case MOUSE:
        if (enablePrefetch && getPageStart() != getLastOffset()) {
          int step = getPageStart() - getLastOffset();

          long now = System.currentTimeMillis();
          long duration = now - getLastRepeatTime();
          setLastRepeatTime(now);

          if (step == getRepeatStep()) {
            if (!isPrefetchPending() && duration <= AsyncProvider.maxRepeatMillis) {
              prefetch(step, (int) duration);
            }
          } else {
            setRepeatStep(step);
          }
        }
        break;

      default:
        resetRepeat();
    }

    super.onDataRequest(event);
  }

  @Override
  public void onFilterChange(final Filter newFilter, final boolean updateActiveRow,
      final Procedure<Boolean> callback) {
    resetRequests();
    Filter flt = getQueryFilter(newFilter);

    Queries.getRowCount(getViewName(), flt, new Queries.IntCallback() {
      @Override
      public void onSuccess(Integer result) {
        if (newFilter == null || BeeUtils.isPositive(result)) {
          acceptFilter(newFilter);
          onRowCount(result, updateActiveRow);
          if (callback != null) {
            callback.call(true);
          }

        } else {
          rejectFilter(newFilter);
          if (callback != null) {
            callback.call(false);
          }
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
  public void onUnload() {
    resetRequests();
    super.onUnload();
  }

  @Override
  public void refresh(final boolean updateActiveRow) {
    resetRequests();
    Global.getCache().remove(getViewName());

    if (hasPaging()) {
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
      requestScheduler.cancel();
      Integer requestId = null;

      for (Map.Entry<Integer, QueryCallback> entry : pendingRequests.entrySet()) {
        if (entry.getValue().getQueryRange().encloses(displayRange)) {
          entry.getValue().setDisplayRange(displayRange);
          entry.getValue().setUpdateActiveRow(updateActiveRow);

          if (pendingRequests.size() > 1) {
            requestId = entry.getKey();
            break;
          } else {
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
        requestScheduler.cancel();
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
    QueryCallback callback = new QueryCallback(queryRange, displayRange, updateActiveRow);

    requestScheduler.scheduleQuery(flt, ord, queryOffset, queryLimit, caching, callback);
  }

  private int averageRpcDuration() {
    return (getRpcCount() > 0) ? (int) (getRpcMillis() / getRpcCount()) : 0;
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
      return Range.closedOpen(offset, offset + limit);
    } else {
      return Range.atLeast(offset);
    }
  }

  private Boolean getDirection(int offset) {
    if (offset == getLastOffset()) {
      return null;
    } else {
      return offset > getLastOffset();
    }
  }

  private int getLastOffset() {
    return lastOffset;
  }

  private long getLastRepeatTime() {
    return lastRepeatTime;
  }

  private int getRepeatStep() {
    return repeatStep;
  }

  private int getRowCount() {
    return getDisplay().getRowCount();
  }

  private int getRpcCount() {
    return rpcCount;
  }

  private long getRpcMillis() {
    return rpcMillis;
  }

  private boolean isPrefetchPending() {
    return prefetchPending;
  }

  private void onResponse(long startTime) {
    long millis = System.currentTimeMillis() - startTime;
    if (startTime > 0 && millis > 0) {
      setRpcCount(getRpcCount() + 1);
      setRpcMillis(getRpcMillis() + millis);
    }
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

  private void prefetch(int step, int duration) {
    int offset = getPageStart();
    int limit = getPageSize();
    int rowCount = getRowCount();
    if (limit <= 0 || limit * AsyncProvider.PREFETCH_SIZE_COEFFICIENT * 2 >= rowCount) {
      return;
    }

    int rpcDuration = BeeUtils.positive(averageRpcDuration(), AsyncProvider.DEFAULT_RPC_DURATION);
    int numberOfSteps = BeeUtils.clamp(rpcDuration / duration + 1,
        AsyncProvider.minPrefetchSteps, AsyncProvider.maxPrefetchSteps);

    int queryLimit = limit * AsyncProvider.PREFETCH_SIZE_COEFFICIENT;

    int startOffset;
    if (step > 0) {
      startOffset = Math.min(offset + numberOfSteps * step, rowCount - limit);
      if (startOffset <= offset) {
        return;
      }
      queryLimit = Math.min(queryLimit, rowCount - startOffset);

    } else {
      startOffset = Math.max(offset + numberOfSteps * step, limit);
      if (startOffset >= offset) {
        return;
      }
      queryLimit = Math.min(queryLimit, startOffset);
    }

    Filter flt = getFilter();
    Order ord = getOrder();

    int from = Global.getCache().firstNotCached(getViewName(), flt, ord, startOffset, queryLimit,
        step > 0);
    if (BeeConst.isUndef(from)) {
      return;
    }

    int queryOffset;
    if (step > 0) {
      queryOffset = from;
      if (queryOffset > startOffset) {
        queryLimit = Math.min(queryLimit, rowCount - queryOffset);
      }

    } else {
      queryOffset = from - queryLimit + 1;
      if (queryOffset < 0) {
        queryOffset = 0;
        queryLimit = from + 1;
      }
    }

    setPrefetchPending(true);
    final long startTime = System.currentTimeMillis();

    Queries.getRowSet(getViewName(), null, flt, ord, queryOffset, queryLimit, CachingPolicy.WRITE,
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            onResponse(startTime);
            setPrefetchPending(false);
          }
        });
  }

  private void resetRepeat() {
    setRepeatStep(0);
  }

  private void resetRequests() {
    requestScheduler.cancel();
    cancelPendingRequests();

    setLastOffset(0);
    resetRepeat();
    setPrefetchPending(false);
  }

  private void setLastOffset(int lastOffset) {
    this.lastOffset = lastOffset;
  }

  private void setLastRepeatTime(long lastRepeatTime) {
    this.lastRepeatTime = lastRepeatTime;
  }

  private void setPrefetchPending(boolean prefetchPending) {
    this.prefetchPending = prefetchPending;
  }

  private void setRepeatStep(int repeatStep) {
    this.repeatStep = repeatStep;
  }

  private void setRpcCount(int rpcCount) {
    this.rpcCount = rpcCount;
  }

  private void setRpcMillis(long rpcMillis) {
    this.rpcMillis = rpcMillis;
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

    if (!hasPaging()) {
      getDisplay().setRowCount(rowCount, true);
    }
    if (updateActiveRow) {
      getDisplay().updateActiveRow(rows);
    }
    getDisplay().setRowData(rows, true);
  }
}
