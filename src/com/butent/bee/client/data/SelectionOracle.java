package com.butent.bee.client.data;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Provides suggestions data management functionality for data changing events.
 */

public class SelectionOracle implements HandlesAllDataEvents, HasViewName {

  /**
   * Requires implementing classes to have a method to handle suggestions events with requests and
   * responses.
   */

  public interface Callback {
    void onSuggestionsReady(Request request, Response response);
  }

  /**
   * Contains fields and methods to handle suggestion related data queries.
   */

  public static class Request {

    private final String query;
    private final Operator searchType;

    private final int offset;
    private final int limit;

    public Request(String query, Operator searchType, int offset, int limit) {
      this.query = query;
      this.searchType = searchType;
      this.offset = offset;
      this.limit = limit;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Request)) {
        return false;
      }
      Request r = (Request) obj;
      return BeeUtils.equalsTrim(getQuery(), r.getQuery())
          && Objects.equal(getSearchType(), r.getSearchType())
          && getOffset() == r.getOffset() && getLimit() == r.getLimit();
    }

    public int getLimit() {
      return limit;
    }

    public int getOffset() {
      return offset;
    }

    public String getQuery() {
      return query;
    }

    public Operator getSearchType() {
      return searchType;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(BeeUtils.trim(getQuery()), getSearchType(), getOffset(), getLimit());
    }
  }

  /**
   * Contains fields and methods to handle suggestion related data responses.
   */

  public static class Response {

    private final Collection<Suggestion> suggestions;
    private final boolean moreSuggestions;

    private Response(Collection<Suggestion> suggestions, boolean moreSuggestions) {
      this.suggestions = suggestions;
      this.moreSuggestions = moreSuggestions;
    }

    public Collection<Suggestion> getSuggestions() {
      return suggestions;
    }

    public boolean hasMoreSuggestions() {
      return moreSuggestions;
    }
  }

  /**
   * Manages suggestion columns and their searchability.
   */

  public static class SelectionColumn {

    private final String name;
    private final boolean searchable;

    public SelectionColumn(String name) {
      this(name, true);
    }

    public SelectionColumn(String name, boolean searchable) {
      this.name = name;
      this.searchable = searchable;
    }

    public String getName() {
      return name;
    }

    public boolean isSearchable() {
      return searchable;
    }
  }

  /**
   * Handles a single row of suggestions.
   */

  public static class Suggestion {

    private final String displayString;
    private final long rowId;
    private final String relValue;

    public Suggestion(String displayString, long rowId, String relValue) {
      this.displayString = displayString;
      this.rowId = rowId;
      this.relValue = relValue;
    }

    public String getDisplayString() {
      return displayString;
    }

    public String getRelValue() {
      return relValue;
    }

    public long getRowId() {
      return rowId;
    }
  }

  /**
   * Contains a list of possible caching settings for selections, from none to full.
   */

  private enum Caching {
    NONE, QUERY, FULL
  }

  /**
   * Manages suggestion requests, which are not yet processed, stores their request and callback
   * information.
   */

  private class PendingRequest {
    private final Request request;
    private final Callback callback;

    private PendingRequest(Request request, Callback callback) {
      this.request = request;
      this.callback = callback;
    }

    private Callback getCallback() {
      return callback;
    }

    private Request getRequest() {
      return request;
    }
  }

  private final RelationInfo relationInfo;

  private final List<String> viewColumns = Lists.newArrayList();
  private final int relIndex;
  
  private final List<Integer> searchColumns = Lists.newArrayList();

  private final Order viewOrder;

  private final CachingPolicy cachingPolicy;
  private final int cachingThreshold;

  private Caching caching = null;

  private BeeRowSet viewData = null;
  private BeeRowSet requestData = null;

  private Request lastRequest = null;

  private PendingRequest pendingRequest = null;

  private final List<BeeColumn> dataColumns = Lists.newArrayList();

  private final List<HandlerRegistration> handlerRegistry = Lists.newArrayList();

  public SelectionOracle(RelationInfo relationInfo, List<SelectionColumn> cols,
      CachingPolicy cachingPolicy, int cachingThreshold) {
    Assert.notNull(relationInfo);
    this.relationInfo = relationInfo;

    String relColumn = relationInfo.getRelColumn();
    if (!containsColumn(cols, relColumn)) {
      getViewColumns().add(relColumn);
      getSearchColumns().add(0);
    }

    if (cols != null) {
      for (SelectionColumn column : cols) {
        String name = column.getName();
        if (!BeeUtils.containsSame(getViewColumns(), name)) {
          getViewColumns().add(name);
          if (column.isSearchable()) {
            getSearchColumns().add(getViewColumns().size() - 1);
          }
        }
      }
    }
    
    this.relIndex = BeeUtils.indexOf(getViewColumns(), relColumn);

    this.viewOrder = new Order(relColumn, true);

    this.cachingPolicy = cachingPolicy;
    this.cachingThreshold = cachingThreshold;

    initColumns();
    initCaching();

    this.handlerRegistry.addAll(BeeKeeper.getBus().registerDataHandler(this));
  }

  public String getViewName() {
    return getRelationInfo().getRelView();
  }

  public void onCellUpdate(CellUpdateEvent event) {
    if (isEventRelevant(event) && BeeUtils.containsSame(getViewColumns(), event.getColumnId())) {
      initViewData();
    }
  }

  public void onMultiDelete(MultiDeleteEvent event) {
    if (isEventRelevant(event)) {
      for (RowInfo rowInfo : event.getRows()) {
        int index = getViewData().getRowIndex(rowInfo.getId());
        if (index >= 0) {
          getViewData().removeRow(index);
        }
      }
    }
  }

  public void onRowDelete(RowDeleteEvent event) {
    if (isEventRelevant(event)) {
      int index = getViewData().getRowIndex(event.getRowId());
      if (index >= 0) {
        getViewData().removeRow(index);
      }
    }
  }

  public void onRowInsert(RowInsertEvent event) {
    if (isEventRelevant(event)) {
      initViewData();
    }
  }

  public void onRowUpdate(RowUpdateEvent event) {
    if (isEventRelevant(event)) {
      initViewData();
    }
  }

  public void onUnload() {
    for (HandlerRegistration entry : getHandlerRegistry()) {
      if (entry != null) {
        entry.removeHandler();
      }
    }
  }

  public void requestSuggestions(Request request, Callback callback) {
    Assert.notNull(request);
    Assert.notNull(callback);

    if (getCaching() == null || getDataColumns().isEmpty()) {
      setPendingRequest(new PendingRequest(request, callback));
      return;
    }
    if (!prepareData(request)) {
      setPendingRequest(new PendingRequest(request, callback));
      return;
    }

    setLastRequest(request);
    processRequest(request, callback);
  }

  public void rotateCaching() {
    int index = getCaching().ordinal();
    if (index >= Caching.values().length - 1) {
      index = 0;
    } else {
      index++;
    }

    Caching value = Caching.values()[index];
    BeeKeeper.getLog().debug("caching", value);
    setCaching(value);
    setLastRequest(null);
  }

  private void checkPendingRequest() {
    if (getPendingRequest() != null) {
      Request request = getPendingRequest().getRequest();
      Callback callback = getPendingRequest().getCallback();
      setPendingRequest(null);
      requestSuggestions(request, callback);
    }
  }

  private boolean containsColumn(Collection<SelectionColumn> cols, String name) {
    if (cols == null) {
      return false;
    }
    for (SelectionColumn column : cols) {
      if (BeeUtils.same(column.getName(), name)) {
        return true;
      }
    }
    return false;
  }

  private Caching getCaching() {
    return caching;
  }

  private CachingPolicy getCachingPolicy() {
    return cachingPolicy;
  }

  private int getCachingThreshold() {
    return cachingThreshold;
  }

  private List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  private Filter getFilter(String query, Operator searchType) {
    if (BeeUtils.isEmpty(query)) {
      return null;
    }
    Filter filter = null;

    for (Integer index : getSearchColumns()) {
      Filter flt = DataUtils.parseExpression(BeeUtils.concat(1,
          getViewColumns().get(index), searchType == null ? "" : searchType.toTextString(), query),
          getDataColumns());

      if (flt == null) {
        continue;
      }
      if (filter == null) {
        filter = flt;
      } else {
        filter = CompoundFilter.or(filter, flt);
      }
    }
    return filter;
  }

  private List<HandlerRegistration> getHandlerRegistry() {
    return handlerRegistry;
  }

  private Request getLastRequest() {
    return lastRequest;
  }

  private PendingRequest getPendingRequest() {
    return pendingRequest;
  }

  private RelationInfo getRelationInfo() {
    return relationInfo;
  }

  private int getRelIndex() {
    return relIndex;
  }

  private BeeRowSet getRequestData() {
    return requestData;
  }

  private List<Integer> getSearchColumns() {
    return searchColumns;
  }

  private List<String> getViewColumns() {
    return viewColumns;
  }

  private BeeRowSet getViewData() {
    return viewData;
  }

  private Order getViewOrder() {
    return viewOrder;
  }

  private void initCaching() {
    CachingPolicy policy = getCachingPolicy();
    if (policy == null || policy == CachingPolicy.NONE) {
      setCaching(Caching.NONE);
      return;
    }
    final int threshold = getCachingThreshold();

    if (threshold <= 0) {
      if (policy == CachingPolicy.FULL) {
        setCaching(Caching.FULL);
      } else {
        setCaching(Caching.QUERY);
      }
      return;
    }

    Queries.getRowCount(getViewName(), new Queries.IntCallback() {
      public void onFailure(String[] reason) {
      }

      public void onSuccess(Integer result) {
        if (BeeUtils.unbox(result) > threshold) {
          setCaching(Caching.QUERY);
        } else {
          setCaching(Caching.FULL);
        }
      }
    });
  }

  private void initColumns() {
    Queries.getRowSet(getViewName(), null, null, null, 0, 1,
        new Queries.RowSetCallback() {
          public void onFailure(String[] reason) {
          }

          public void onSuccess(BeeRowSet result) {
            getDataColumns().clear();
            for (BeeColumn column : result.getColumns()) {
              getDataColumns().add(column);
            }
            checkPendingRequest();
          }
        });
  }

  private void initViewData() {
    Queries.getRowSet(getViewName(), getViewColumns(), getViewOrder(),
        new Queries.RowSetCallback() {
          public void onFailure(String[] reason) {
          }

          public void onSuccess(BeeRowSet result) {
            setViewData(result);
            checkPendingRequest();
          }
        });
  }

  private boolean isCachingEnabled() {
    return getCaching() != null && getCaching() != Caching.NONE;
  }

  private boolean isEventRelevant(DataEvent event) {
    return event != null && BeeUtils.same(event.getViewName(), getViewName())
        && getCaching() == Caching.FULL;
  }

  private boolean prepareData(final Request request) {
    if (getLastRequest() != null) {
      if (isCachingEnabled()) {
        if (BeeUtils.equalsTrim(request.getQuery(), getLastRequest().getQuery())
            && request.getSearchType() == getLastRequest().getSearchType()) {
          return true;
        }
      } else if (request.equals(getLastRequest())) {
        return true;
      }
    }

    final Filter filter = getFilter(request.getQuery(), request.getSearchType());
    if (filter == null && !isCachingEnabled()) {
      setRequestData(null);
      return true;
    }

    if (getCaching() == Caching.FULL) {
      if (getViewData() == null) {
        return false;
      }
      if (getRequestData() == null) {
        setRequestData(new BeeRowSet(getViewData().getColumns()));
      } else {
        getRequestData().getRows().clear();
      }

      for (BeeRow row : getViewData().getRows()) {
        if (filter == null || filter.isMatch(getViewData().getColumns(), row)) {
          getRequestData().addRow(row);
        }
      }
      return true;
    }

    int offset = request.getOffset();
    int limit = request.getLimit();
    if (isCachingEnabled()) {
      offset = BeeConst.UNDEF;
      limit = BeeConst.UNDEF;
    } else {
      limit++;
    }

    Queries.getRowSet(getViewName(), getViewColumns(), filter, getViewOrder(), offset, limit,
        new Queries.RowSetCallback() {
          public void onFailure(String[] reason) {
          }

          public void onSuccess(BeeRowSet result) {
            if (getPendingRequest() == null) {
              return;
            }
            if (request.equals(getPendingRequest().getRequest())) {
              setRequestData(result);
              setLastRequest(request);
              Callback callback = getPendingRequest().getCallback();
              setPendingRequest(null);
              processRequest(request, callback);
            } else {
              checkPendingRequest();
            }
          }
        });

    return false;
  }

  private void processRequest(Request request, Callback callback) {
    int offset = request.getOffset();
    int limit = request.getLimit();

    List<Suggestion> suggestions = Lists.newArrayList();
    boolean hasMore = false;

    if (getRequestData() != null && !getRequestData().isEmpty()) {
      int rowCount = getRequestData().getNumberOfRows();
      int start = isCachingEnabled() ? Math.max(offset, 0) : 0;
      int end = (limit > 0) ? Math.min(start + limit, rowCount) : rowCount;

      if (start < end) {
        for (int i = start; i < end; i++) {
          BeeRow row = getRequestData().getRow(i);
          suggestions.add(new Suggestion(toDisplay(row), row.getId(),
              row.getString(getRelIndex())));
        }
        hasMore = end < rowCount;
      }
    }

    Response response = new Response(suggestions, hasMore);
    callback.onSuggestionsReady(request, response);
  }

  private void setCaching(Caching caching) {
    Assert.notNull(caching);
    this.caching = caching;

    if (caching == Caching.FULL) {
      initViewData();
    }
  }

  private void setLastRequest(Request lastRequest) {
    this.lastRequest = lastRequest;
  }

  private void setPendingRequest(PendingRequest pendingRequest) {
    this.pendingRequest = pendingRequest;
  }

  private void setRequestData(BeeRowSet requestData) {
    this.requestData = requestData;
  }

  private void setViewData(BeeRowSet viewData) {
    this.viewData = viewData;
  }

  private String toDisplay(BeeRow row) {
    if (row == null) {
      return null;
    }

    String separator = ", ";
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < row.getNumberOfCells(); i++) {
      String value = row.getString(i);
      if (!BeeUtils.isEmpty(value)) {
        if (sb.length() > 0) {
          sb.append(separator);
        }
        sb.append(value.trim());
      }
    }
    return sb.toString();
  }
}