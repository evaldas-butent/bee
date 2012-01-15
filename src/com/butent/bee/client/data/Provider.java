package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataRequestEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

/**
 * Enables to manage ranges of data shown in user interface tables.
 */

public abstract class Provider implements SortEvent.Handler, HandlesAllDataEvents, HasViewName {

  private final HasDataTable display;

  private final String viewName;
  private final String idColumnName;
  private final String versionColumnName;

  private final List<BeeColumn> columns;

  private final List<HandlerRegistration> handlerRegistry = Lists.newArrayList();

  private boolean cacheEnabled = true;

  private final Filter dataFilter;
  private final Map<String, Filter> parentFilters = Maps.newHashMap();
  private Filter userFilter = null;

  private Order order = null;

  protected Provider(HasDataTable display, String viewName, List<BeeColumn> columns,
      String idColumnName, String versionColumnName, Filter dataFilter) {
    Assert.notNull(display);
    this.display = display;
    this.viewName = viewName;
    this.columns = columns;
    this.idColumnName = idColumnName;
    this.versionColumnName = versionColumnName;
    this.dataFilter = dataFilter;

    this.handlerRegistry.add(display.addDataRequestHandler(new DataRequestEvent.Handler() {
      public void onDataRequest(DataRequestEvent event) {
        onRequest(false);
      }
    }));

    this.handlerRegistry.add(display.addSortHandler(this));
    this.handlerRegistry.addAll(BeeKeeper.getBus().registerDataHandler(this));
  }

  public void disableCache() {
    setCacheEnabled(false);
  }

  public void enableCache() {
    setCacheEnabled(true);
  }

  public int getColumnIndex(String columnId) {
    return DataUtils.getColumnIndex(columnId, getColumns());
  }

  public List<BeeColumn> getColumns() {
    return columns;
  }
  
  public String getIdColumnName() {
    return idColumnName;
  }

  public Order getOrder() {
    return order;
  }

  public Filter getQueryFilter(Filter filter) {
    List<Filter> lst = Lists.newArrayList();

    if (getDataFilter() != null) {
      lst.add(getDataFilter());
    }
    if (!getParentFilters().isEmpty()) {
      for (Filter flt : getParentFilters().values()) {
        if (flt != null) {
          lst.add(flt);
        }
      }
    }

    if (filter != null) {
      lst.add(filter);
    }
    return Filter.and(lst);
  }

  public Filter getUserFilter() {
    return userFilter;
  }

  public String getVersionColumnName() {
    return versionColumnName;
  }

  public String getViewName() {
    return viewName;
  }

  public boolean isCacheEnabled() {
    return cacheEnabled;
  }

  public void onCellUpdate(CellUpdateEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      getDisplay().onCellUpdate(event);
    }
  }

  public void onFilterChanged(Filter newFilter, int rowCount) {
    setUserFilter(newFilter);
    getDisplay().setRowCount(rowCount, true);
    goTop();
  }

  public void onMultiDelete(MultiDeleteEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      disableCache();
      getDisplay().onMultiDelete(event);
      onRequest(false);
      enableCache();
    }
  }

  public void onRowDelete(RowDeleteEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      disableCache();
      getDisplay().onRowDelete(event);
      onRequest(false);
      enableCache();
    }
  }

  public abstract void onRowInsert(RowInsertEvent event);

  public void onRowUpdate(RowUpdateEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      getDisplay().onRowUpdate(event);
    }
  }

  public abstract void onSort(SortEvent event);

  public void onUnload() {
    for (HandlerRegistration entry : handlerRegistry) {
      if (entry != null) {
        entry.removeHandler();
      }
    }
  }

  public void refresh() {
    if (BeeUtils.isEmpty(getViewName())) {
      return;
    }
    startLoading();
    Global.getCache().removeQuietly(getViewName());

    final Filter flt = getFilter();
    Queries.getRowCount(getViewName(), flt, new Queries.IntCallback() {
      public void onFailure(String[] reason) {
      }

      public void onSuccess(Integer result) {
        if (result <= 0) {
          BeeKeeper.getLog().warning(getViewName(), flt, "refresh: row count", result);
        }
        getDisplay().setRowCount(result, true);
        onRefresh();
      }
    });
  }

  public abstract void requery(boolean updateActiveRow);

  public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public void setParentFilter(String key, Filter filter) {
    Assert.notEmpty(key);
    if (filter == null) {
      getParentFilters().remove(key);
    } else {
      getParentFilters().put(key, filter);
    }
  }

  public void setUserFilter(Filter userFilter) {
    this.userFilter = userFilter;
  }

  protected HasDataTable getDisplay() {
    return display;
  }

  protected Filter getFilter() {
    return getQueryFilter(getUserFilter());
  }

  protected int getPageSize() {
    return getDisplay().getPageSize();
  }

  protected int getPageStart() {
    return getDisplay().getPageStart();
  }

  protected void goTop() {
    getDisplay().setPageStart(0, true, false);
    onRequest(true);
  }

  protected abstract void onRefresh();

  protected abstract void onRequest(boolean updateActiveRow);

  protected void startLoading() {
    getDisplay().fireLoadingStateChange(LoadingStateChangeEvent.LoadingState.LOADING);
  }

  private Filter getDataFilter() {
    return dataFilter;
  }

  private Map<String, Filter> getParentFilters() {
    return parentFilters;
  }
}
