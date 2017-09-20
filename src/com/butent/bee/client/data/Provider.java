package com.butent.bee.client.data;

import com.google.common.collect.ImmutableMap;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.DataRequestEvent;
import com.butent.bee.client.event.logical.SortEvent;
import com.butent.bee.client.screen.HandlesStateChange;
import com.butent.bee.client.view.search.FilterConsumer;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.ModificationPreviewer;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.rights.RightsState;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.NavigationOrigin;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enables to manage ranges of data shown in user interface tables.
 */

public abstract class Provider implements SortEvent.Handler, HandlesAllDataEvents, HasViewName,
    DataRequestEvent.Handler, FilterConsumer, HandlesActions, HandlesStateChange {

  private static final String DEFAULT_PARENT_FILTER_KEY = "f1";

  public static Map<String, Filter> createDefaultParentFilters(Filter filter) {
    return ImmutableMap.of(DEFAULT_PARENT_FILTER_KEY, filter);
  }

  private HasDataTable display;
  private final HasDataProvider presenter;

  private final ModificationPreviewer modificationPreviewer;
  private final NotificationListener notificationListener;

  private final String viewName;
  private final List<BeeColumn> columns;

  private final String idColumnName;
  private final String versionColumnName;

  private final List<HandlerRegistration> displayRegistry = new ArrayList<>();
  private final List<HandlerRegistration> dataRegistry = new ArrayList<>();

  private final Filter immutableFilter;
  private final Map<String, Filter> parentFilters = new HashMap<>();
  private Filter userFilter;

  private final String dataOptions;

  private Order order;

  private final Set<RightsState> rightsStates = new HashSet<>();

  protected Provider(HasDataTable display, HasDataProvider presenter,
      ModificationPreviewer modificationPreviewer, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, String idColumnName, String versionColumnName,
      Filter immutableFilter, Map<String, Filter> parentFilters, Filter userFilter,
      String dataOptions) {

    this.display = display;
    this.presenter = presenter;
    this.modificationPreviewer = modificationPreviewer;
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

    this.dataOptions = dataOptions;

    this.dataRegistry.addAll(BeeKeeper.getBus().registerDataHandler(this, false));

    bindDisplay(display);
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

  public Filter getFilter() {
    return getQueryFilter(getUserFilter());
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
    List<Filter> lst = new ArrayList<>();

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

  public Set<RightsState> getRightsStates() {
    return rightsStates;
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
    if (presenter != null) {
      presenter.handleAction(action);
    }
  }

  public boolean hasFilter() {
    return getImmutableFilter() != null || !getParentFilters().isEmpty() || getUserFilter() != null;
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (event.hasView(getViewName()) && (modificationPreviewer == null
        || modificationPreviewer.previewCellUpdate(event))) {

      getDisplay().onCellUpdate(event);
    }
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (event.hasView(getViewName()) && (modificationPreviewer == null
        || modificationPreviewer.previewDataChange(event))) {

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
  public void onRowInsert(RowInsertEvent event) {
    if (event.hasView(getViewName()) && modificationPreviewer != null) {
      modificationPreviewer.previewRowInsert(event);
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (event.hasView(getViewName()) && (modificationPreviewer == null
        || modificationPreviewer.previewRowUpdate(event))) {

      getDisplay().onRowUpdate(event);
    }
  }

  @Override
  public void onStateChange(State state) {
    if (presenter != null) {
      presenter.onStateChange(state);
    }
  }

  public void onUnload() {
    EventUtils.clearRegistry(displayRegistry);
    EventUtils.clearRegistry(dataRegistry);
  }

  public abstract void refresh(boolean preserveActiveRow);

  public boolean setDefaultParentFilter(Filter filter) {
    return setParentFilter(DEFAULT_PARENT_FILTER_KEY, filter);
  }

  public void setDisplay(HasDataTable display) {
    this.display = display;
    bindDisplay(display);
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public boolean setParentFilter(String key, Filter filter) {
    Assert.notEmpty(key);
    if (filter == null) {
      return getParentFilters().remove(key) != null;
    } else {
      return !filter.equals(getParentFilters().put(key, filter));
    }
  }

  public void setUserFilter(Filter userFilter) {
    this.userFilter = userFilter;
  }

  public void toggleRightsState(RightsState rightsState) {
    Assert.notNull(rightsState);

    if (rightsStates.contains(rightsState)) {
      rightsStates.remove(rightsState);
    } else {
      rightsStates.add(rightsState);
    }
  }

  protected HasDataTable getDisplay() {
    return display;
  }

  protected int getPageSize() {
    return getDisplay().getPageSize();
  }

  protected int getPageStart() {
    return getDisplay().getPageStart();
  }

  protected Collection<Property> getQueryOptions() {
    Collection<Property> result = new HashSet<>();

    if (!rightsStates.isEmpty()) {
      result.add(new Property(Service.VAR_RIGHTS, EnumUtils.joinIndexes(rightsStates)));
    }
    if (!BeeUtils.isEmpty(dataOptions)) {
      result.add(new Property(Service.VAR_VIEW_EVENT_OPTIONS, dataOptions));
    }

    return result;
  }

  protected void goTop() {
    getDisplay().setPageStart(0, true, false, NavigationOrigin.SYSTEM);
    onRequest(true);
  }

  protected boolean hasPaging() {
    return getPageSize() > 0;
  }

  protected boolean hasQueryOptions() {
    return !rightsStates.isEmpty() || !BeeUtils.isEmpty(dataOptions);
  }

  protected abstract void onRequest(boolean preserveActiveRow);

  protected boolean previewMultiDelete(MultiDeleteEvent event) {
    return event.hasView(getViewName()) && (modificationPreviewer == null
        || modificationPreviewer.previewMultiDelete(event));
  }

  protected boolean previewRowDelete(RowDeleteEvent event) {
    return event.hasView(getViewName()) && (modificationPreviewer == null
        || modificationPreviewer.previewRowDelete(event));
  }

  protected void rejectFilter(Filter filter, boolean notify) {
    if (filter != null && notify && notificationListener != null) {
      if (Global.isDebug()) {
        notificationListener.notifyWarning("no rows found", filter.toString());
      } else {
        notificationListener.notifyWarning(Localized.dictionary().nothingFound());
      }
    }
  }

  private void bindDisplay(HasDataTable d) {
    if (!displayRegistry.isEmpty()) {
      EventUtils.clearRegistry(displayRegistry);
    }

    HandlerRegistration hr = d.addDataRequestHandler(this);
    if (hr != null) {
      displayRegistry.add(hr);
    }

    hr = d.addSortHandler(this);
    if (hr != null) {
      displayRegistry.add(hr);
    }
  }

  private Map<String, Filter> getParentFilters() {
    return parentFilters;
  }
}
