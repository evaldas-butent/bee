package com.butent.bee.client.presenter;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Element;

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
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.FooterView;
import com.butent.bee.client.view.GridContainerImpl;
import com.butent.bee.client.view.GridContainerView;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.HasSearch;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.search.FilterHandler;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Consumer;
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
import com.butent.bee.shared.data.filter.FilterInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GridPresenter extends AbstractPresenter implements ReadyForInsertEvent.Handler,
    ReadyForUpdateEvent.Handler, SaveChangesEvent.Handler, HasSearch, HasDataProvider,
    HasActiveRow, HasGridView, HasViewName {

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

  private Filter lastFilter = null;

  public GridPresenter(GridDescription gridDescription, int rowCount, BeeRowSet rowSet,
      Provider.Type providerType, CachingPolicy cachingPolicy, Collection<UiOption> uiOptions) {
    this(gridDescription, null, rowCount, rowSet, providerType, cachingPolicy, uiOptions,
        null, null, null, null, null);
  }

  public GridPresenter(GridDescription gridDescription, String relColumn, int rowCount,
      BeeRowSet rowSet, Provider.Type providerType, CachingPolicy cachingPolicy,
      Collection<UiOption> uiOptions, GridInterceptor gridInterceptor, Filter immutableFilter,
      Map<String, Filter> initialFilters, Order order, GridFactory.GridOptions gridOptions) {

    if (gridInterceptor != null) {
      gridInterceptor.setGridPresenter(this);
    }

    this.gridContainer = createView(gridDescription, rowSet.getColumns(), relColumn,
        rowCount, rowSet, order, gridInterceptor, uiOptions, gridOptions);

    this.dataProvider = createProvider(gridContainer, gridDescription.getViewName(),
        rowSet.getColumns(), gridDescription.getIdName(), gridDescription.getVersionName(),
        immutableFilter, initialFilters, order, rowSet, providerType, cachingPolicy);

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
  public Collection<SearchView> getSearchers() {
    Collection<SearchView> searchers;

    if (getMainView() instanceof HasSearch) {
      searchers = ((HasSearch) getMainView()).getSearchers();
    } else {
      searchers = Sets.newHashSet();
    }
    return searchers;
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
        Global.inputString("Options", new StringCallback() {
          @Override
          public void onSuccess(String value) {
            getGridView().applyOptions(value);
          }
        });
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
        handleFilter();
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
        Collection<SearchView> searchers = getSearchers();
        for (SearchView searcher : searchers) {
          searcher.clearFilter();
        }

        updateFilter(null);
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

    BeeRowSet rs = new BeeRowSet(new BeeColumn(event.getColumn().getType(), columnId));
    rs.setViewName(getViewName());
    rs.addRow(rowId, version, new String[] {event.getOldValue()});
    rs.getRow(0).preliminaryUpdate(0, newValue);

    final boolean rowMode = event.isRowMode();

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
      Queries.updateRow(rs, rowCallback);
    } else {
      Queries.updateCell(rs, rowCallback);
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
      Filter filter = ViewHelper.getFilter(this, getDataProvider());
      if (filter != null && getGridView().getGrid().getRowCount() <= 0) {
        setLastFilter(null);
        getDataProvider().onFilterChange(null, updateActiveRow, null);
      } else if (Objects.equal(filter, getLastFilter())) {
        getDataProvider().refresh(updateActiveRow);
      } else {
        getDataProvider().onFilterChange(filter, updateActiveRow, null);
      }
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

    Collection<SearchView> searchers = getSearchers();

    if (!searchers.isEmpty()) {
      FilterHandler handler = new FilterHandler() {
        @Override
        public Filter getEffectiveFilter(ImmutableSet<String> exclusions) {
          return getDataProvider().getQueryFilter(ViewHelper.getFilter(GridPresenter.this,
              getDataProvider(), exclusions));
        }

        @Override
        public void onFilterChange(Consumer<Boolean> callback) {
          GridPresenter.this.updateFilter(callback);
        }
      };

      for (SearchView search : searchers) {
        search.setFilterHandler(handler);
      }
    }

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
      Map<String, Filter> initialFilters, Order order, BeeRowSet rowSet,
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
      String relColumn, int rowCount, BeeRowSet rowSet, Order order,
      GridInterceptor gridInterceptor, Collection<UiOption> uiOptions,
      GridFactory.GridOptions gridOptions) {

    GridContainerView view = new GridContainerImpl();
    view.create(gridDescription, columns, relColumn, rowCount, rowSet, order, gridInterceptor,
        uiOptions, gridOptions);

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

  private FooterView getFooter() {
    return gridContainer.getFooter();
  }

  private GridInterceptor getGridInterceptor() {
    return getGridView().getGridInterceptor();
  }

  private Filter getLastFilter() {
    return lastFilter;
  }

  private void handleFilter() {
    String label;
    String serialized;

    List<FilterInfo> columnFilters = getGridView().getColumnFilters();
    if (BeeUtils.isEmpty(columnFilters)) {
      label = null;
      serialized = null;

    } else {
      List<String> labels = Lists.newArrayList();
      for (FilterInfo filterInfo : columnFilters) {
        labels.add(BeeUtils.joinWords(filterInfo.getCaption(), filterInfo.getLabel()));
      }

      label = BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, labels);
      serialized = Codec.beeSerialize(columnFilters);
    }

    Element relativeTo = (getHeader() == null) ? null : getHeader().asWidget().getElement();

    Global.getFilters().handle(gridContainer.getSupplierKey(), label, serialized, relativeTo,
        new Consumer<String>() {
          @Override
          public void accept(String input) {
            String arr[] = Codec.beeDeserializeCollection(input);
            List<FilterInfo> filters = Lists.newArrayList();
            for (String s : arr) {
              filters.add(FilterInfo.restore(s));
            }

            getGridView().setColumnFilters(filters);
            updateFilter(null);
          }
        });
  }

  private void setLastFilter(Filter lastFilter) {
    this.lastFilter = lastFilter;
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

  private void updateFilter(Consumer<Boolean> callback) {
    Filter filter = ViewHelper.getFilter(this, getDataProvider());

    if (!Objects.equal(filter, getLastFilter())) {
      setLastFilter(filter);
      getDataProvider().onFilterChange(filter, true, callback);

      FooterView footer = getFooter();
      if (footer != null) {
        footer.showFilterDelete(filter != null);
      }
    }
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
