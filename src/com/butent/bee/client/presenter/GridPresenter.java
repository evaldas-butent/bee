package com.butent.bee.client.presenter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.AsyncProvider;
import com.butent.bee.client.data.CachedProvider;
import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.GridContainerImpl;
import com.butent.bee.client.view.GridContainerView;
import com.butent.bee.client.view.HasSearch;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
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

public class GridPresenter extends AbstractPresenter implements ReadyForInsertEvent.Handler,
    ReadyForUpdateEvent.Handler, SaveChangesEvent.Handler, HasSearch {

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

        if (BeeUtils.isEmpty(getViewName())) {
          getDataProvider().onRowDelete(new RowDeleteEvent(getViewName(), rowId));
          afterDelete(rowId);
        } else {
          Queries.deleteRow(getViewName(), rowId, version, new Queries.IntCallback() {
            public void onFailure(String[] reason) {
              setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
              showFailure("Delete Row", reason);
            }

            public void onSuccess(Integer result) {
              BeeKeeper.getBus().fireEvent(new RowDeleteEvent(getViewName(), rowId));
              afterDelete(rowId);
            }
          });
        }

      } else if (count > 1) {
        final long[] rowIds = new long[count];
        int i = 0;
        for (RowInfo rowInfo : rows) {
          rowIds[i] = rowInfo.getId();
          i++;
        }

        if (BeeUtils.isEmpty(getViewName())) {
          getDataProvider().onMultiDelete(new MultiDeleteEvent(getViewName(), rows));
          afterMulti(rowIds);
        } else {
          Queries.deleteRows(getViewName(), rows, new Queries.IntCallback() {
            public void onFailure(String[] reason) {
              showFailure("Delete Rows", reason);
              setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
            }

            public void onSuccess(Integer result) {
              BeeKeeper.getBus().fireEvent(new MultiDeleteEvent(getViewName(), rows));
              afterMulti(rowIds);
              showInfo("Deleted " + result + " rows");
            }
          });
        }
      }
    }

    private void afterMulti(long[] rowIds) {
      for (long rowId : rowIds) {
        afterDelete(rowId);
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
      GridDescription gridDescription, GridCallback gridCallback,
      Map<String, Filter> initialFilters, Collection<UiOption> options) {
    if (gridCallback != null) {
      gridCallback.setGridPresenter(this);
    }

    this.gridContainer = createView(gridDescription, rowSet.getColumns(), rowCount, rowSet,
        gridCallback, options);

    this.dataProvider =
        createProvider(gridContainer, viewName, rowSet.getColumns(),
            gridDescription.getIdName(), gridDescription.getVersionName(),
            gridDescription.getFilter(), initialFilters, gridDescription.getOrder(),
            rowSet, async, gridDescription.getCachingPolicy());

    bind();
  }

  public void addRow() {
    if (getGridCallback() != null && !getGridCallback().beforeAddRow(this)) {
      return;
    }
    getView().getContent().startNewRow();
  }

  public void close() {
    if (getGridCallback() != null && !getGridCallback().onClose(this)) {
      return;
    }
    BeeKeeper.getScreen().closeView(getView());
  }

  public void deleteRow(IsRow row, boolean confirm) {
    Assert.notNull(row);
    int mode;
    if (getGridCallback() != null) {
      mode = getGridCallback().beforeDeleteRow(this, row, confirm);
    } else {
      mode = GridCallback.DELETE_DEFAULT;
    }

    if (mode == GridCallback.DELETE_CANCEL) {
      return;
    }

    DeleteCallback deleteCallback = new DeleteCallback(row.getId(), row.getVersion());
    if (mode == GridCallback.DELETE_SILENT || mode == GridCallback.DELETE_DEFAULT && !confirm) {
      deleteCallback.execute();
    } else {
      String message = (getGridCallback() == null) ? null : getGridCallback().getDeleteRowMessage();
      Global.getMsgBoxen().confirm(BeeUtils.ifString(message, "Delete Row ?"), deleteCallback,
          StyleUtils.NAME_SCARY);
    }
  }

  public IsRow getActiveRow() {
    return getView().getContent().getActiveRowData();
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

  public Collection<SearchView> getSearchers() {
    Collection<SearchView> searchers;

    if (getView() instanceof HasSearch) {
      searchers = ((HasSearch) getView()).getSearchers();
    } else {
      searchers = null;
    }
    return searchers;
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
    if (getGridCallback() != null && !getGridCallback().beforeAction(action, this)) {
      return;
    }

    switch (action) {
      case ADD:
        if (getView().isEnabled()) {
          addRow();
        }
        break;

      case CLOSE:
        close();
        break;

      case CONFIGURE:
        Global.inputString("Options", new DialogCallback<String>() {
          @Override
          public void onSuccess(String value) {
            getView().getContent().applyOptions(value);
          }
        });
        break;

      case DELETE:
        if (getView().isEnabled()) {
          IsRow row = getActiveRow();
          if (row != null && getView().getContent().isRowEditable(row, true)) {
            Collection<RowInfo> selectedRows = getView().getContent().getSelectedRows();
            boolean isActiveRowSelected = getView().getContent().isRowSelected(row.getId());

            if (selectedRows.isEmpty() || isActiveRowSelected && selectedRows.size() == 1) {
              deleteRow(row, true);
            } else {
              deleteRows(row, selectedRows);
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

      default:
        BeeKeeper.getLog().info(action, "not implemented");
    }

    if (getGridCallback() != null) {
      getGridCallback().afterAction(action, this);
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

    if (BeeUtils.isEmpty(getViewName())) {
      getDataProvider().onCellUpdate(new CellUpdateEvent(getViewName(), rowId, version, columnId,
          getDataProvider().getColumnIndex(columnId), newValue));
      return;
    }

    BeeRowSet rs = new BeeRowSet(new BeeColumn(event.getColumn().getType(), columnId));
    rs.setViewName(getViewName());
    rs.addRow(rowId, version, new String[] {event.getOldValue()});
    rs.getRow(0).preliminaryUpdate(0, newValue);

    final boolean rowMode = event.isRowMode();

    Queries.update(rs, rowMode, new Queries.RowCallback() {
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
    getView().setViewPresenter(null);

    for (HandlerRegistration hr : filterChangeHandlers) {
      hr.removeHandler();
    }
    filterChangeHandlers.clear();

    getDataProvider().onUnload();
  }

  public void refresh() {
    if (getGridCallback() != null) {
      getGridCallback().beforeRefresh(this);
    }

    Filter filter = ViewHelper.getFilter(this, getDataProvider());
    if (Objects.equal(filter, getLastFilter())) {
      getDataProvider().refresh();
    } else {
      applyFilter(filter);
    }
  }

  public void requery(boolean updateActiveRow) {
    if (getGridCallback() != null) {
      getGridCallback().beforeRequery(this);
    }
    getDataProvider().requery(updateActiveRow);
  }

  private void afterDelete(long rowId) {
    if (getGridCallback() != null) {
      getGridCallback().afterDeleteRow(rowId);
    }
  }

  private void applyFilter(Filter filter) {
    setLastFilter(filter);
    Queries.getRowCount(getViewName(), getDataProvider().getQueryFilter(filter),
        new FilterCallback(filter));
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
      Map<String, Filter> initialFilters, Order order, BeeRowSet rowSet, boolean isAsync,
      CachingPolicy cachingPolicy) {
    Provider provider;
    GridView display = view.getContent();

    if (BeeUtils.isEmpty(viewName)) {
      provider = new LocalProvider(display.getGrid(), columns, dataFilter, rowSet);
    } else if (isAsync) {
      provider = new AsyncProvider(display.getGrid(), viewName, columns,
          idColumnName, versionColumnName, dataFilter);
      if (cachingPolicy != null) {
        ((AsyncProvider) provider).setCachingPolicy(cachingPolicy);
      }
    } else {
      provider = new CachedProvider(display.getGrid(), viewName, columns,
          idColumnName, versionColumnName, dataFilter, rowSet);
    }

    if (initialFilters != null) {
      for (Map.Entry<String, Filter> entry : initialFilters.entrySet()) {
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

  private void deleteRows(final IsRow activeRow, final Collection<RowInfo> selectedRows) {
    int size = selectedRows.size();

    List<String> options = Lists.newArrayList();
    if (getGridCallback() != null) {
      Pair<String, String> message = getGridCallback().getDeleteRowsMessage(size);
      if (message != null) {
        options.add(message.getA());
        options.add(message.getB());
      }
    }

    if (options.isEmpty()) {
      options.add("Delete current row");
      options.add(BeeUtils.concat(1, "Delete", size, "selected row" + (size > 1 ? "s" : "")));
    }

    Global.choice("Delete", null, options, new DialogCallback<Integer>() {
      public void onSuccess(Integer value) {
        if (value == 0) {
          deleteRow(activeRow, false);
        } else if (value == 1) {
          int mode;
          if (getGridCallback() == null) {
            mode = GridCallback.DELETE_DEFAULT;
          } else {
            mode = getGridCallback().beforeDeleteRows(GridPresenter.this, activeRow, selectedRows);
          }
          if (mode == GridCallback.DELETE_CANCEL) {
            return;
          }

          DeleteCallback deleteCallback = new DeleteCallback(selectedRows);
          deleteCallback.execute();
        }
      }
    }, 2, BeeConst.UNDEF, DialogConstants.CANCEL, new WidgetInitializer() {
      public Widget initialize(Widget widget, String name) {
        if (BeeUtils.same(name, DialogConstants.WIDGET_DIALOG)) {
          widget.addStyleName(StyleUtils.NAME_SUPER_SCARY);
        }
        return widget;
      }
    });
  }

  private GridCallback getGridCallback() {
    return getView().getContent().getGridCallback();
  }

  private String getViewName() {
    return getDataProvider().getViewName();
  }

  private void setLastFilter(Filter lastFilter) {
    this.lastFilter = lastFilter;
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
    Filter filter = ViewHelper.getFilter(this, getDataProvider());
    if (Objects.equal(filter, getLastFilter())) {
      showInfo("filter not changed", BeeUtils.transform(filter));
    } else {
      applyFilter(filter);
    }
  }
}
