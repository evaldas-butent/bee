package com.butent.bee.client.presenter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.AsyncProvider;
import com.butent.bee.client.data.CachedProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.GridContainerImpl;
import com.butent.bee.client.view.GridContainerView;
import com.butent.bee.client.view.HasSearch;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains necessary methods for implementing grid presentation on the client side (view, filters,
 * content etc).
 */

public class GridPresenter implements Presenter, ReadyForInsertEvent.Handler,
    ReadyForUpdateEvent.Handler, SaveChangesEvent.Handler {

  private class DeleteCallback extends BeeCommand {
    private final Collection<RowInfo> rows;

    private DeleteCallback(Collection<RowInfo> rows) {
      this.rows = rows;
    }

    private DeleteCallback(long rowId, long version) {
      this(Lists.newArrayList(new RowInfo(rowId, version)));
    }

    @Override
    public void execute() {
      Assert.notNull(rows);
      int count = rows.size();
      Assert.isPositive(count);

      setLoadingState(LoadingStateChangeEvent.LoadingState.LOADING);

      if (count == 1) {
        RowInfo rowInfo = BeeUtils.peek(rows);
        final long rowId = rowInfo.getId();
        long version = rowInfo.getVersion();

        Queries.deleteRow(getViewName(), rowId, version, new Queries.IntCallback() {
          public void onFailure(String[] reason) {
            setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
            showFailure("Delete Row", reason);
          }

          public void onSuccess(Integer result) {
            BeeKeeper.getBus().fireEvent(new RowDeleteEvent(getViewName(), rowId));
          }
        });

      } else if (count > 1) {
        Queries.deleteRows(getViewName(), rows, new Queries.IntCallback() {
          public void onFailure(String[] reason) {
            showFailure("Delete Rows", reason);
            setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
          }

          public void onSuccess(Integer result) {
            BeeKeeper.getBus().fireEvent(new MultiDeleteEvent(getViewName(), rows));
            showInfo("Deleted " + result + " rows");
          }
        });
      }
    }
  }

  private class FilterCallback implements Queries.IntCallback {
    private Filter filter;

    private FilterCallback(Filter filter) {
      this.filter = filter;
    }

    public void onFailure(String[] reason) {
      showFailure("Filter", reason);
    }

    public void onSuccess(Integer result) {
      if (!Objects.equal(filter, getLastFilter())) {
        BeeKeeper.getLog().warning("filter not the same");
        BeeKeeper.getLog().warning(getLastFilter());
        return;
      }

      if (result > 0) {
        getDataProvider().onFilterChanged(filter, result);
      } else if (filter != null) {
        showWarning("Filter: " + filter.transform(), "no data found");
      }
    }
  }

  private final GridContainerView gridContainer;
  private final Provider dataProvider;

  private final Set<HandlerRegistration> filterChangeHandlers = Sets.newHashSet();
  private Filter lastFilter = null;

  public GridPresenter(String viewName, int rowCount, BeeRowSet rowSet, boolean async,
      GridDescription gridDescription, GridCallback gridCallback, Collection<UiOption> options) {

    this.gridContainer = createView(gridDescription, rowSet.getColumns(), rowCount, rowSet,
        gridCallback, options);
    this.dataProvider =
        createProvider(gridContainer, viewName, rowSet.getColumns(),
            gridDescription.getIdName(), gridDescription.getVersionName(),
            gridDescription.getFilter(), gridDescription.getParentFilters(), gridDescription
                .getOrder(),
            rowSet, async, gridDescription.getCachingPolicy());

    bind();
  }

  public void deleteRow(IsRow row) {
    Assert.notNull(row);
    int z;
    if (getGridCallback() != null) {
      z = getGridCallback().beforeDeleteRow(this, row);
      if (z < 0) {
        return;
      }
    } else {
      z = 0;
    }

    DeleteCallback deleteCallback = new DeleteCallback(row.getId(), row.getId());
    if (z > 0) {
      deleteCallback.execute();
    } else {
      Global.getMsgBoxen().confirm("Delete Row ?", deleteCallback, StyleUtils.NAME_SCARY);
    }
  }

  public List<BeeColumn> getDataColumns() {
    return getDataProvider().getColumns();
  }

  public Provider getDataProvider() {
    return dataProvider;
  }

  public Filter getLastFilter() {
    return lastFilter;
  }

  public GridContainerView getView() {
    return gridContainer;
  }

  public Widget getWidget() {
    return getView().asWidget();
  }

  @Override
  public void handleAction(Action action) {
    Assert.notNull(action);

    switch (action) {
      case CLOSE:
        BeeKeeper.getScreen().closeView(getView());
        break;

      case CONFIGURE:
        String options = Window.prompt("Options", "");
        if (!BeeUtils.isEmpty(options)) {
          getView().getContent().applyOptions(options);
        }
        break;

      case DELETE:
        if (getView().isEnabled()) {
          IsRow row = getView().getContent().getActiveRowData();
          if (row != null && getView().getContent().isRowEditable(row.getId(), true)) {
            if (getView().getContent().isRowSelected(row.getId())) {
              deleteRows(row, getView().getContent().getSelectedRows());
            } else {
              deleteRow(row);
            }
          }
        }
        break;

      case REFRESH:
        refresh();
        break;

      case REQUERY:
        requery(true);
        break;

      case ADD:
        if (getView().isEnabled()) {
          getView().getContent().startNewRow();
        }
        break;

      default:
        BeeKeeper.getLog().info(action, "not implemented");
    }
  }

  public void onReadyForInsert(ReadyForInsertEvent event) {
    setLoadingState(LoadingStateChangeEvent.LoadingState.LOADING);

    Queries.insert(getViewName(), event.getColumns(), event.getValues(), new Queries.RowCallback() {
      public void onFailure(String[] reason) {
        setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
        showFailure("Insert Row", reason);
        getView().getContent().finishNewRow(null);
      }

      public void onSuccess(BeeRow result) {
        BeeKeeper.getBus().fireEvent(new RowInsertEvent(getViewName(), result));
        getView().getContent().finishNewRow(result);
      }
    });
  }

  public void onReadyForUpdate(ReadyForUpdateEvent event) {
    final long rowId = event.getRowValue().getId();
    final long version = event.getRowValue().getVersion();
    final String columnId = event.getColumn().getLabel();
    final String newValue = event.getNewValue();

    BeeRowSet rs = new BeeRowSet(new BeeColumn(event.getColumn().getType(), columnId));
    rs.setViewName(getViewName());
    rs.addRow(rowId, version, new String[] {event.getOldValue()});
    rs.getRow(0).preliminaryUpdate(0, newValue);

    final boolean rowMode = event.isRowMode();

    Queries.update(rs, rowMode,
        new Queries.RowCallback() {
          public void onFailure(String[] reason) {
            getView().getContent().refreshCellContent(rowId, columnId);
            showFailure("Update Cell", reason);
          }

          public void onSuccess(BeeRow row) {
            BeeKeeper.getLog().info("cell updated:", getViewName(), rowId, columnId, newValue);
            if (rowMode) {
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(getViewName(), row));
            } else {
              BeeKeeper.getBus().fireEvent(
                  new CellUpdateEvent(getViewName(), rowId, row.getVersion(), columnId,
                      getDataProvider().getColumnIndex(columnId), newValue));
            }
          }
        });
  }

  public void onSaveChanges(SaveChangesEvent event) {
    final long rowId = event.getRowId();

    Queries.update(getViewName(), rowId, event.getVersion(), event.getColumns(),
        event.getOldValues(), event.getNewValues(), new Queries.RowCallback() {
          public void onFailure(String[] reason) {
            showFailure("Save Changes", reason);
          }

          public void onSuccess(BeeRow row) {
            BeeKeeper.getLog().info("changes saved", getViewName(), rowId);
            BeeKeeper.getBus().fireEvent(new RowUpdateEvent(getViewName(), row));
          }
        });
  }

  public void onViewUnload() {
    if (BeeKeeper.getScreen().isTemporaryDetach()) {
      return;
    }
    getView().setViewPresenter(null);

    for (HandlerRegistration hr : filterChangeHandlers) {
      hr.removeHandler();
    }
    filterChangeHandlers.clear();

    getDataProvider().onUnload();
  }
  
  public void refresh() {
    if (getGridCallback() != null) {
      getGridCallback().beforeRefresh();
    }
    getDataProvider().refresh();
  }

  public void requery(boolean updateActiveRow) {
    if (getGridCallback() != null) {
      getGridCallback().beforeRequery();
    }
    getDataProvider().requery(updateActiveRow);
  }

  private void bind() {
    GridContainerView view = getView();
    view.setViewPresenter(this);
    view.bind();

    Collection<SearchView> searchers = getSearchers();
    if (searchers != null) {
      for (SearchView search : searchers) {
        filterChangeHandlers.add(search.addChangeHandler(new ChangeHandler() {
          public void onChange(ChangeEvent event) {
            updateFilter();
          }
        }));
      }
    }

    view.getContent().addReadyForUpdateHandler(this);
    view.getContent().addReadyForInsertHandler(this);

    view.getContent().addSaveChangesHandler(this);
  }

  private Provider createProvider(GridContainerView view, String viewName, List<BeeColumn> columns,
      String idColumnName, String versionColumnName, Filter dataFilter,
      Map<String, Filter> parentFilters, Order order, BeeRowSet rowSet, boolean isAsync,
      CachingPolicy cachingPolicy) {
    Provider provider;
    GridView display = view.getContent();

    if (isAsync) {
      provider = new AsyncProvider(display.getGrid(), viewName, columns,
          idColumnName, versionColumnName, dataFilter);
      if (cachingPolicy != null) {
        ((AsyncProvider) provider).setCachingPolicy(cachingPolicy);
      }
    } else {
      provider = new CachedProvider(display.getGrid(), viewName, columns,
          idColumnName, versionColumnName, dataFilter, rowSet);
    }

    if (parentFilters != null) {
      for (Map.Entry<String, Filter> entry : parentFilters.entrySet()) {
        String key = entry.getKey();
        Filter value = entry.getValue();
        if (!BeeUtils.isEmpty(key) && value != null) {
          provider.setParentFilter(key, value);
        }
      }
    }

    if (order != null) {
      provider.setOrder(order);
    }
    return provider;
  }

  private GridContainerView createView(GridDescription gridDescription, List<BeeColumn> columns,
      int rowCount, BeeRowSet rowSet, GridCallback gridCallback, Collection<UiOption> options) {

    GridContainerView view = new GridContainerImpl();
    view.create(gridDescription, columns, rowCount, rowSet, gridCallback, options);

    return view;
  }

  private void deleteRows(IsRow activeRow, Collection<RowInfo> selectedRows) {
    if (selectedRows.size() <= 1) {
      deleteRow(activeRow);
      return;
    }

    int z;
    if (getGridCallback() != null) {
      z = getGridCallback().beforeDeleteRows(this, activeRow, selectedRows);
      if (z < 0) {
        return;
      }
    } else {
      z = 0;
    }

    DeleteCallback deleteCallback = new DeleteCallback(selectedRows);
    if (z > 0) {
      deleteCallback.execute();
    } else {
      Global.getMsgBoxen().confirm(BeeUtils.concat(1, "Delete", selectedRows.size(), "rows"),
          Lists.newArrayList("SRSLY ?"), deleteCallback, StyleUtils.NAME_SUPER_SCARY);
    }
  }

  private GridCallback getGridCallback() {
    return getView().getContent().getGridCallback();
  }

  private Collection<SearchView> getSearchers() {
    Collection<SearchView> searchers;

    if (getView() instanceof HasSearch) {
      searchers = ((HasSearch) getView()).getSearchers();
    } else {
      searchers = null;
    }
    return searchers;
  }

  private String getViewName() {
    return getDataProvider().getViewName();
  }

  private void setLoadingState(LoadingStateChangeEvent.LoadingState loadingState) {
    if (loadingState != null) {
      getView().getContent().getGrid().fireLoadingStateChange(loadingState);
    }
  }

  private void showFailure(String activity, String... reasons) {
    List<String> messages = Lists.newArrayList(activity);
    if (reasons != null) {
      messages.addAll(Lists.newArrayList(reasons));
    }
    getView().getContent().notifySevere(messages.toArray(new String[0]));
  }

  private void showInfo(String... messages) {
    getView().getContent().notifyInfo(messages);
  }

  private void showWarning(String... messages) {
    getView().getContent().notifyWarning(messages);
  }

  private void updateFilter() {
    Collection<SearchView> searchers = getSearchers();
    Assert.notNull(searchers);

    List<Filter> filters = Lists.newArrayListWithCapacity(searchers.size());
    for (SearchView search : searchers) {
      Filter flt = search.getFilter(getDataColumns(), getDataProvider().getIdColumnName(),
          getDataProvider().getVersionColumnName());
      if (flt != null && !filters.contains(flt)) {
        filters.add(flt);
      }
    }

    Filter filter;
    switch (filters.size()) {
      case 0:
        filter = null;
        break;
      case 1:
        filter = filters.get(0);
        break;
      default:
        filter = Filter.and(filters);
    }

    if (Objects.equal(filter, getLastFilter())) {
      BeeKeeper.getLog().info("filter not changed", filter);
      return;
    }

    lastFilter = filter;
    Queries.getRowCount(getViewName(), getDataProvider().getQueryFilter(filter),
        new FilterCallback(filter));
  }
}
