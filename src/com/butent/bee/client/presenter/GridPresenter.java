package com.butent.bee.client.presenter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.AsyncProvider;
import com.butent.bee.client.data.CachedProvider;
import com.butent.bee.client.data.HasActiveRow;
import com.butent.bee.client.data.HasDataProvider;
import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.GridContainerImpl;
import com.butent.bee.client.view.GridContainerView;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridFilterManager;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridSettings;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.HasViewName;
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
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GridPresenter extends AbstractPresenter implements ReadyForInsertEvent.Handler,
    ReadyForUpdateEvent.Handler, SaveChangesEvent.Handler, HasDataProvider, HasActiveRow,
    HasGridView, HasViewName {

  private class DeleteCallback extends ConfirmationCallback {
    private final IsRow activeRow;
    private final Collection<RowInfo> rows;

    private DeleteCallback(IsRow activeRow, Collection<RowInfo> rows) {
      this.activeRow = activeRow;
      this.rows = rows;
    }

    @Override
    public void onConfirm() {
      int count = (rows == null) ? 0 : rows.size();
      GridInterceptor gcb = getGridInterceptor();

      if (gcb != null) {
        if ((count == 0
            ? gcb.beforeDeleteRow(GridPresenter.this, activeRow)
            : gcb.beforeDeleteRows(GridPresenter.this, activeRow, rows))
          == GridInterceptor.DeleteMode.CANCEL) {
          return;
        }
      }

      if (count == 0) {
        final long rowId = activeRow.getId();
        long version = activeRow.getVersion();

        if (BeeUtils.isEmpty(getViewName())) {
          getDataProvider().onRowDelete(new RowDeleteEvent(getViewName(), rowId));
          afterDelete(rowId);
        } else {
          Queries.deleteRow(getViewName(), rowId, version, new Queries.IntCallback() {
            @Override
            public void onFailure(String... reason) {
              showFailure("Error deleting row", reason);
            }

            @Override
            public void onSuccess(Integer result) {
              BeeKeeper.getBus().fireEvent(new RowDeleteEvent(getViewName(), rowId));
              afterDelete(rowId);
            }
          });
        }
      } else {
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
            @Override
            public void onFailure(String... reason) {
              showFailure("Error deleting rows", reason);
            }

            @Override
            public void onSuccess(Integer result) {
              BeeKeeper.getBus().fireEvent(new MultiDeleteEvent(getViewName(), rows));
              afterMulti(rowIds);
              showInfo("Išmesta " + result + " eil.");
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

  private static final BeeLogger logger = LogUtils.getLogger(GridPresenter.class);

  private final GridContainerView gridContainer;
  private final Provider dataProvider;
  private final GridFilterManager filterManager;

  public GridPresenter(GridDescription gridDescription, GridView gridView, int rowCount,
      BeeRowSet rowSet, Provider.Type providerType, CachingPolicy cachingPolicy,
      Collection<UiOption> uiOptions) {
    this(gridDescription, gridView, rowCount, rowSet, providerType, cachingPolicy, uiOptions,
        null, null, null, null, null, null, null);
  }

  public GridPresenter(GridDescription gridDescription, GridView gridView, int rowCount,
      BeeRowSet rowSet, Provider.Type providerType, CachingPolicy cachingPolicy,
      Collection<UiOption> uiOptions, GridInterceptor gridInterceptor,
      Filter immutableFilter, Map<String, Filter> parentFilters,
      List<Map<String, String>> userFilterValues, Filter userFilter,
      Order order, GridFactory.GridOptions gridOptions) {

    if (gridInterceptor != null) {
      gridInterceptor.setGridPresenter(this);
    }

    this.gridContainer = createView(gridDescription, gridView, rowCount, userFilter,
        gridInterceptor, uiOptions, gridOptions);

    this.dataProvider = createProvider(gridContainer, gridDescription.getViewName(),
        rowSet.getColumns(), gridDescription.getIdName(), gridDescription.getVersionName(),
        immutableFilter, parentFilters, userFilter, order, rowSet, providerType, cachingPolicy);

    this.filterManager = new GridFilterManager();
    if (userFilterValues != null && !userFilterValues.isEmpty()) {
      filterManager.setFilter(gridContainer.getGridView().getGrid(), userFilterValues);
    }

    bind();
  }

  public void deleteRow(IsRow row, boolean confirm) {
    Assert.notNull(row);

    List<String> messages = (getGridInterceptor() != null)
        ? getGridInterceptor().getDeleteRowMessage(row)
        : AbstractGridInterceptor.DELETE_ROW_MESSAGE;

    GridInterceptor.DeleteMode mode = BeeUtils.isEmpty(messages)
        ? GridInterceptor.DeleteMode.SILENT : GridInterceptor.DeleteMode.DEFAULT;

    DeleteCallback deleteCallback = new DeleteCallback(row, null);

    if (mode == GridInterceptor.DeleteMode.SILENT
        || mode == GridInterceptor.DeleteMode.DEFAULT && !confirm) {
      deleteCallback.onConfirm();
    } else {
      Global.confirmDelete(getCaption(), Icon.WARNING, messages, deleteCallback);
    }
  }

  public void deleteRows(final IsRow activeRow, final Collection<RowInfo> selectedRows) {
    int size = selectedRows.size();
    List<String> options = Lists.newArrayList();

    Pair<String, String> defMsg = AbstractGridInterceptor.deleteRowsMessage(size);
    Pair<String, String> message =
        (getGridInterceptor() != null) ? getGridInterceptor().getDeleteRowsMessage(size) : defMsg;

    if (message != null) {
      options.add(BeeUtils.notEmpty(message.getA(), defMsg.getA()));
      options.add(BeeUtils.notEmpty(message.getB(), defMsg.getB()));
    }

    if (options.isEmpty()) {
      DeleteCallback deleteCallback = new DeleteCallback(activeRow, selectedRows);
      deleteCallback.onConfirm();

    } else {
      options.add(Localized.constants.cancel());

      Global.getMsgBoxen().display(getCaption(), Icon.ALARM, Lists.newArrayList("Išmesti ?"),
          options, 2, new ChoiceCallback() {
            @Override
            public void onSuccess(int value) {
              if (value == 0) {
                deleteRow(activeRow, false);

              } else if (value == 1) {
                DeleteCallback deleteCallback = new DeleteCallback(activeRow, selectedRows);
                deleteCallback.onConfirm();
              }
            }
          }, BeeConst.UNDEF, null, StyleUtils.FontSize.XX_LARGE.getClassName(),
          StyleUtils.FontSize.MEDIUM.getClassName(), null);
    }
  }

  @Override
  public IsRow getActiveRow() {
    return getGridView().getActiveRow();
  }

  @Override
  public String getCaption() {
    return gridContainer.getCaption();
  }

  public List<BeeColumn> getDataColumns() {
    return getDataProvider().getColumns();
  }

  @Override
  public Provider getDataProvider() {
    return dataProvider;
  }

  @Override
  public GridView getGridView() {
    return gridContainer.getGridView();
  }

  @Override
  public HeaderView getHeader() {
    return gridContainer.getHeader();
  }

  @Override
  public View getMainView() {
    return gridContainer;
  }

  @Override
  public String getViewName() {
    return getDataProvider().getViewName();
  }

  @Override
  public IdentifiableWidget getWidget() {
    return getMainView();
  }

  @Override
  public void handleAction(Action action) {
    Assert.notNull(action);
    if (getGridView().hasNotifications()) {
      return;
    }
    if (getGridInterceptor() != null && !getGridInterceptor().beforeAction(action, this)) {
      return;
    }

    switch (action) {
      case ADD:
        if (getMainView().isEnabled()) {
          addRow();
        }
        break;

      case BOOKMARK:
        Global.getFavorites().bookmark(getViewName(), getActiveRow(), getDataColumns(),
            gridContainer.getFavorite());
        break;

      case CLOSE:
        close();
        break;

      case CONFIGURE:
        GridSettings.handle(getGridView().getGridKey(), getGridView().getGrid(),
            getHeader().asWidget());
        break;

      case DELETE:
        if (getMainView().isEnabled()) {
          IsRow row = getActiveRow();

          if (row != null && getGridView().isRowEditable(row, true)) {
            Collection<RowInfo> selectedRows = getGridView().getSelectedRows(SelectedRows.EDITABLE);

            GridInterceptor.DeleteMode mode = getDeleteMode(row, selectedRows);

            if (GridInterceptor.DeleteMode.SINGLE.equals(mode)) {
              deleteRow(row, true);
            } else if (GridInterceptor.DeleteMode.MULTI.equals(mode)) {
              deleteRows(row, selectedRows);
            }
          }
        }
        break;

      case FILTER:
        filterManager.handleFilter(getGridView().getGrid(), getMainView().getElement(),
            new Consumer<Filter>() {
              @Override
              public void accept(Filter input) {
                onFilterChange(input);
              }
            });
        break;

      case PRINT:
        if (getGridView().getGrid().getRowCount() > 0) {
          Printer.print(gridContainer);
        }
        break;

      case REFRESH:
        refresh(true);
        break;

      case REMOVE_FILTER:
        filterManager.clearFilter(getGridView().getGrid());
        onFilterChange(null);
        break;

      default:
        logger.info(action, "not implemented");
    }

    if (getGridInterceptor() != null) {
      getGridInterceptor().afterAction(action, this);
    }
  }

  @Override
  public void onReadyForInsert(final ReadyForInsertEvent event) {
    Queries.insert(getViewName(), event.getColumns(), event.getValues(), event.getChildren(),
        new RowCallback() {
          @Override
          public void onFailure(String... reason) {
            if (event.getCallback() == null) {
              showFailure("Insert Row", reason);
            } else {
              event.getCallback().onFailure(reason);
            }
          }

          @Override
          public void onSuccess(BeeRow result) {
            BeeKeeper.getBus().fireEvent(new RowInsertEvent(getViewName(), result));
            if (event.getCallback() != null) {
              event.getCallback().onSuccess(result);
            }
          }
        });
  }

  @Override
  public boolean onReadyForUpdate(final ReadyForUpdateEvent event) {
    final long rowId = event.getRowValue().getId();
    final long version = event.getRowValue().getVersion();
    final String columnId = event.getColumn().getId();
    final String newValue = event.getNewValue();

    final CellSource source = CellSource.forColumn(event.getColumn(),
        getDataProvider().getColumnIndex(columnId));

    if (BeeUtils.isEmpty(getViewName())) {
      getDataProvider().onCellUpdate(new CellUpdateEvent(getViewName(), rowId, version,
          source, newValue));
      return true;
    }

    BeeRowSet rowSet = event.getRowSet(getViewName(), getDataColumns());
    event.getRowValue().reset();

    final boolean rowMode = event.isRowMode() || rowSet.getNumberOfColumns() > 1;

    RowCallback rowCallback = new RowCallback() {
      @Override
      public void onFailure(String... reason) {
        if (event.getCallback() != null) {
          event.getCallback().onFailure(reason);
        }
      }

      @Override
      public void onSuccess(BeeRow row) {
        if (event.getCallback() != null) {
          event.getCallback().onSuccess(row);
        }

        if (rowMode) {
          BeeKeeper.getBus().fireEvent(new RowUpdateEvent(getViewName(), row));
        } else {
          String value = row.getString(0);
          BeeKeeper.getBus().fireEvent(new CellUpdateEvent(getViewName(), rowId, row.getVersion(),
              source, value));
        }
      }
    };

    if (rowMode) {
      Queries.updateRow(rowSet, rowCallback);
    } else {
      Queries.updateCell(rowSet, rowCallback);
    }
    return true;
  }

  @Override
  public void onSaveChanges(final SaveChangesEvent event) {
    Queries.update(getViewName(), event.getRowId(), event.getVersion(), event.getColumns(),
        event.getOldValues(), event.getNewValues(), event.getChildren(), new RowCallback() {
          @Override
          public void onFailure(String... reason) {
            if (event.getCallback() == null) {
              showFailure("Save Changes", reason);
            } else {
              event.getCallback().onFailure(reason);
            }
          }

          @Override
          public void onSuccess(BeeRow row) {
            BeeKeeper.getBus().fireEvent(new RowUpdateEvent(getViewName(), row));
            if (event.getCallback() != null) {
              event.getCallback().onSuccess(row);
            }
          }
        });
  }

  @Override
  public void onViewUnload() {
    getMainView().setViewPresenter(null);

    getDataProvider().onUnload();

    super.onViewUnload();
  }

  public void refresh(boolean updateActiveRow) {
    if (getGridInterceptor() != null) {
      getGridInterceptor().beforeRefresh(this);
    }

    if (getGridView().likeAMotherlessChild()) {
      if (getGridView().getGrid().getRowCount() > 0) {
        getDataProvider().clear();
      }

    } else {
      getDataProvider().refresh(updateActiveRow);
    }
  }

  private void addRow() {
    if (getGridView().likeAMotherlessChild() && !validateParent()) {
      return;
    }

    if (getGridInterceptor() != null && !getGridInterceptor().beforeAddRow(this)) {
      return;
    }
    getGridView().startNewRow();
  }

  private void afterDelete(long rowId) {
    if (getGridInterceptor() != null) {
      getGridInterceptor().afterDeleteRow(rowId);
    }
  }

  private void bind() {
    GridContainerView view = gridContainer;
    view.setViewPresenter(this);
    view.bind();

    view.getGridView().addReadyForUpdateHandler(this);
    view.getGridView().addReadyForInsertHandler(this);

    view.getGridView().addSaveChangesHandler(this);
  }

  private void close() {
    if (getGridInterceptor() != null && !getGridInterceptor().onClose(this)) {
      return;
    }
    BeeKeeper.getScreen().closeWidget(getMainView());
  }

  private Provider createProvider(GridContainerView view, String viewName, List<BeeColumn> columns,
      String idColumnName, String versionColumnName, Filter immutableFilter,
      Map<String, Filter> parentFilters, Filter userFilter, Order order, BeeRowSet rowSet,
      Provider.Type providerType, CachingPolicy cachingPolicy) {

    if (providerType == null) {
      return null;
    }

    Provider provider;
    CellGrid display = view.getGridView().getGrid();
    NotificationListener notificationListener = view.getGridView();

    switch (providerType) {
      case ASYNC:
        provider = new AsyncProvider(display, notificationListener, viewName, columns,
            idColumnName, versionColumnName, immutableFilter, cachingPolicy);
        break;

      case CACHED:
        provider = new CachedProvider(display, notificationListener, viewName, columns,
            idColumnName, versionColumnName, immutableFilter, rowSet);
        break;

      case LOCAL:
        provider = new LocalProvider(display, notificationListener, viewName, columns,
            immutableFilter, rowSet);
        break;

      default:
        Assert.untouchable();
        provider = null;
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

    if (userFilter != null) {
      provider.setUserFilter(userFilter);
    }

    if (order != null) {
      provider.setOrder(order);
    }
    return provider;
  }

  private GridContainerView createView(GridDescription gridDescription, GridView gridView,
      int rowCount, Filter userFilter, GridInterceptor gridInterceptor,
      Collection<UiOption> uiOptions, GridFactory.GridOptions gridOptions) {

    GridContainerView view = new GridContainerImpl();
    view.create(gridDescription, gridView, rowCount, userFilter, gridInterceptor, uiOptions,
        gridOptions);

    return view;
  }

  private GridInterceptor.DeleteMode getDeleteMode(IsRow row, Collection<RowInfo> selected) {
    GridInterceptor.DeleteMode mode =
        selected.isEmpty() || selected.size() == 1 && getGridView().isRowSelected(row.getId())
            ? GridInterceptor.DeleteMode.SINGLE : GridInterceptor.DeleteMode.MULTI;

    if (getGridInterceptor() == null) {
      return mode;
    } else {
      return getGridInterceptor().getDeleteMode(this, row, selected, mode);
    }
  }

  private GridInterceptor getGridInterceptor() {
    return getGridView().getGridInterceptor();
  }

  private void onFilterChange(Filter filter) {
    if (Objects.equal(getDataProvider().getUserFilter(), filter)) {
      return;
    }

    HeaderView header = getHeader();
    if (header != null && header.hasAction(Action.REMOVE_FILTER)) {
      header.showAction(Action.REMOVE_FILTER, filter != null);
    }

    getDataProvider().onFilterChange(filter);
  }

  private void showFailure(String activity, String... reasons) {
    List<String> messages = Lists.newArrayList(activity);
    if (reasons != null) {
      messages.addAll(Lists.newArrayList(reasons));
    }
    getGridView().notifySevere(ArrayUtils.toArray(messages));
  }

  private void showInfo(String... messages) {
    getGridView().notifyInfo(messages);
  }

  private boolean validateParent() {
    FormView form = UiHelper.getForm(getWidget().asWidget());
    if (form == null) {
      return true;
    }

    if (!form.validate(form, true)) {
      return false;
    }

    if (form.getViewPresenter() instanceof HasGridView) {
      GridView rootGrid = ((HasGridView) form.getViewPresenter()).getGridView();
      if (rootGrid != null && !rootGrid.validateFormData(form, form, true)) {
        return false;
      }
    }
    return true;
  }
}
