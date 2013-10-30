package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.event.logical.DataRequestEvent;
import com.butent.bee.client.event.logical.SortEvent;
import com.butent.bee.client.view.search.FilterConsumer;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.NavigationOrigin;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

/**
 * Enables to manage ranges of data shown in user interface tables.
 */

public abstract class Provider implements SortEvent.Handler, HandlesAllDataEvents, HasViewName,
    DataRequestEvent.Handler, FilterConsumer, HandlesActions {

  public enum Type {
    ASYNC, CACHED, LOCAL
  }

  private final HasDataTable display;
  private final HandlesActions actionHandler;
  private final NotificationListener notificationListener;

  private final String viewName;
  private final List<BeeColumn> columns;

  private final String idColumnName;
  private final String versionColumnName;

  private final List<HandlerRegistration> handlerRegistry = Lists.newArrayList();

  private final Filter immutableFilter;
  private final Map<String, Filter> parentFilters = Maps.newHashMap();
  private Filter userFilter;

  private Order order;

  protected Provider(HasDataTable display, HandlesActions actionHandler,
      NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, String idColumnName, String versionColumnName,
      Filter immutableFilter, Map<String, Filter> parentFilters, Filter userFilter) {

    this.display = display;
    this.actionHandler = actionHandler;
    this.notificationListener = notificationListener;

    this.viewName = viewName;
    this.columns = columns;

    this.idColumnName = idColumnName;
    this.versionColumnName = versionColumnName;

    this.immutableFilter = immutableFilter;
    
    if (parentFilters != null) {
      for (Map.Entry<String, Filter> entry : parentFilters.entrySet()) {
        String key = entry.getKey();
        Filter value = entry.getValue();
        if (!BeeUtils.isEmpty(key) && value != null) {
          setParentFilter(key, value);
        }
      }
    }
    
    this.userFilter = userFilter;

    this.handlerRegistry.add(display.addDataRequestHandler(this));

    this.handlerRegistry.add(display.addSortHandler(this));
    this.handlerRegistry.addAll(BeeKeeper.getBus().registerDataHandler(this, false));
  }

  public void clear() {
    getDisplay().reset();
    getDisplay().setPageStart(0, false, false, NavigationOrigin.SYSTEM);
    getDisplay().setRowCount(0, true);
    getDisplay().setRowData(null, true);
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

  public Filter getImmutableFilter() {
    return immutableFilter;
  }

  public Order getOrder() {
    return order;
  }

  public Filter getQueryFilter(Filter filter) {
    List<Filter> lst = Lists.newArrayList();

    if (getImmutableFilter() != null) {
      lst.add(getImmutableFilter());
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
  
  @Override
  public String getViewName() {
    return viewName;
  }

  @Override
  public void handleAction(Action action) {
    if (actionHandler != null) {
      actionHandler.handleAction(action);
    }
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      getDisplay().onCellUpdate(event);
    }
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (event.hasView(getViewName())) {
      if (event.hasReset()) {
        getDisplay().reset();
      }
      if (event.hasRefresh()) {
        handleAction(Action.REFRESH);
      }
    }
  }

  @Override
  public void onDataRequest(DataRequestEvent event) {
    onRequest(false);
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      getDisplay().onRowUpdate(event);
    }
  }

  public void onUnload() {
    for (HandlerRegistration entry : handlerRegistry) {
      if (entry != null) {
        entry.removeHandler();
      }
    }
  }

  public abstract void refresh(boolean updateActiveRow);

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
    getDisplay().setPageStart(0, true, false, NavigationOrigin.SYSTEM);
    onRequest(true);
  }

  protected boolean hasPaging() {
    return getPageSize() > 0;
  }

  protected abstract void onRequest(boolean updateActiveRow);

  protected void rejectFilter(Filter filter, boolean notify) {
    if (filter != null && notify && notificationListener != null) {
      if (Global.isDebug()) {
        notificationListener.notifyWarning("no rows found", filter.toString());
      } else {
        notificationListener.notifyWarning(Localized.getConstants().nothingFound());
      }
    }
  }

  private Map<String, Filter> getParentFilters() {
    return parentFilters;
  }
}
