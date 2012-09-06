package com.butent.bee.client.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.JsData;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.cell.HtmlCell;
import com.butent.bee.client.grid.column.BooleanColumn;
import com.butent.bee.client.grid.column.CurrencyColumn;
import com.butent.bee.client.grid.column.DataColumn;
import com.butent.bee.client.grid.column.DateColumn;
import com.butent.bee.client.grid.column.DateTimeColumn;
import com.butent.bee.client.grid.column.DecimalColumn;
import com.butent.bee.client.grid.column.DoubleColumn;
import com.butent.bee.client.grid.column.IntegerColumn;
import com.butent.bee.client.grid.column.LongColumn;
import com.butent.bee.client.grid.column.RowIdColumn;
import com.butent.bee.client.grid.column.TextColumn;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.RenderableCell;
import com.butent.bee.client.render.RenderableColumn;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.PropertiesData;
import com.butent.bee.shared.data.StringMatrix;
import com.butent.bee.shared.data.TableColumn;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.ui.CellType;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class GridFactory {

  public static class GridOptions {

    private String caption = null;
    private String filter = null;
    private String order = null;

    private GridOptions(String caption, String filter, String order) {
      super();
      this.caption = caption;
      this.filter = filter;
      this.order = order;
    }

    public String getCaption() {
      return caption;
    }

    public String getFilter() {
      return filter;
    }

    public String getOrder() {
      return order;
    }

    public boolean hasFilter() {
      return !BeeUtils.isEmpty(getFilter());
    }

    public boolean hasOrder() {
      return !BeeUtils.isEmpty(getOrder());
    }

    public void setCaption(String caption) {
      this.caption = caption;
    }

    public void setFilter(String filter) {
      this.filter = filter;
    }

    public void setOrder(String order) {
      this.order = order;
    }
  }

  public interface PresenterCallback {
    void onCreate(GridPresenter presenter);
  }

  public static final PresenterCallback SHOW = new PresenterCallback() {
    public void onCreate(GridPresenter presenter) {
      if (presenter != null) {
        BeeKeeper.getScreen().updateActivePanel(presenter.getWidget());
      }
    }
  };

  private static final Map<String, GridDescription> descriptionCache = Maps.newHashMap();
  private static final Map<String, GridCallback> gridCallbacks = Maps.newHashMap();

  private static Widget loadingWidget = null;

  public static void clearDescriptionCache() {
    descriptionCache.clear();
  }

  public static Cell<String> createCell(CellType cellType) {
    Assert.notNull(cellType);

    switch (cellType) {
      case HTML:
        return new HtmlCell();
      default:
        return new TextCell();
    }
  }

  public static DataColumn<?> createColumn(IsColumn dataColumn, int index) {
    return createColumn(dataColumn, index, null);
  }

  public static DataColumn<?> createColumn(IsColumn dataColumn, int index, CellType cellType) {
    if (cellType != null) {
      return new TextColumn(createCell(cellType), index, dataColumn);
    }

    ValueType type = dataColumn.getType();
    if (type == null) {
      return new TextColumn(index, dataColumn);
    }

    switch (type) {
      case BOOLEAN:
        return new BooleanColumn(index, dataColumn);
      case DATE:
        return new DateColumn(index, dataColumn);
      case DATETIME:
        return new DateTimeColumn(index, dataColumn);
      case NUMBER:
        return new DoubleColumn(index, dataColumn);
      case INTEGER:
        return new IntegerColumn(index, dataColumn);
      case LONG:
        return new LongColumn(index, dataColumn);
      case DECIMAL:
        if (dataColumn.getScale() == 2) {
          return new CurrencyColumn(index, dataColumn);
        } else {
          return new DecimalColumn(index, dataColumn);
        }
      default:
        return new TextColumn(index, dataColumn);
    }
  }

  public static void createGrid(String gridName, Collection<UiOption> uiOptions,
      GridOptions gridOptions, PresenterCallback presenterCallback) {
    createGrid(gridName, getGridCallback(gridName), uiOptions, gridOptions, presenterCallback);
  }

  public static void createGrid(String gridName, final GridCallback gridCallback,
      final Collection<UiOption> uiOptions, final GridOptions gridOptions,
      final PresenterCallback presenterCallback) {
    Assert.notEmpty(gridName);
    Assert.notNull(presenterCallback);

    getGrid(gridName, new Callback<GridDescription>() {
      @Override
      public void onSuccess(GridDescription result) {
        Assert.notNull(result);
        if (gridCallback != null && !gridCallback.onLoad(result)) {
          return;
        }
        getInitialRowSet(result, gridCallback, presenterCallback, uiOptions, gridOptions);
      }
    });
  }

  public static DataColumn<?> createRenderableColumn(AbstractCellRenderer renderer,
      IsColumn dataColumn, int index, CellType cellType) {
    Cell<String> cell = (cellType == null) ? new RenderableCell() : createCell(cellType);
    return new RenderableColumn(cell, index, dataColumn, renderer);
  }

  @SuppressWarnings("unchecked")
  public static IsTable<?, ?> createTable(Object data, String... columnLabels) {
    Assert.notNull(data);
    IsTable<?, ?> table = null;

    if (data instanceof IsTable) {
      table = (IsTable<?, ?>) data;

    } else if (data instanceof String[][]) {
      table = new StringMatrix<TableColumn>((String[][]) data, columnLabels);

    } else if (data instanceof JsArrayString) {
      table = new JsData<TableColumn>((JsArrayString) data, columnLabels);

    } else if (data instanceof List) {
      Object el = BeeUtils.getQuietly((List<?>) data, 0);

      if (el instanceof ExtendedProperty) {
        table = new ExtendedPropertiesData((List<ExtendedProperty>) data, columnLabels);
      } else if (el instanceof Property) {
        table = new PropertiesData((List<Property>) data, columnLabels);
      } else if (el instanceof String[]) {
        table = new StringMatrix<TableColumn>((List<String[]>) data, columnLabels);
      }

    } else if (data instanceof Map) {
      table = new PropertiesData((Map<?, ?>) data, columnLabels);
    }

    Assert.notNull(table, "createTable: data not recognized");
    return table;
  }

  public static void getGrid(String name, Callback<GridDescription> callback) {
    getGrid(name, callback, false);
  }

  public static void getGrid(final String name, final Callback<GridDescription> callback,
      boolean reload) {
    Assert.notEmpty(name);
    Assert.notNull(callback);

    if (!reload && isGridDescriptionCached(name)) {
      callback.onSuccess(descriptionCache.get(gridDescriptionKey(name)));
      return;
    }

    BeeKeeper.getRpc().sendText(Service.GET_GRID, name, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasResponse(GridDescription.class)) {
          GridDescription gridDescription =
              GridDescription.restore((String) response.getResponse());
          callback.onSuccess(gridDescription);
          if (!BeeUtils.isFalse(gridDescription.getCacheDescription())) {
            descriptionCache.put(gridDescriptionKey(name), gridDescription);
          }
        } else {
          callback.onFailure(response.getErrors());
          descriptionCache.put(gridDescriptionKey(name), null);
        }
      }
    });
  }

  public static GridCallback getGridCallback(String gridName) {
    Assert.notEmpty(gridName);
    GridCallback callback = gridCallbacks.get(BeeUtils.normalize(gridName));
    if (callback != null) {
      GridCallback instance = callback.getInstance();
      if (instance != null) {
        return instance;
      }
    }
    return callback;
  }

  public static GridOptions getGridOptions(Map<String, String> attributes) {
    if (BeeUtils.isEmpty(attributes)) {
      return null;
    }

    String caption = attributes.get(UiConstants.ATTR_CAPTION);
    String filter = attributes.get(UiConstants.ATTR_FILTER);
    String order = attributes.get(UiConstants.ATTR_ORDER);

    if (BeeUtils.allEmpty(caption, filter, order)) {
      return null;
    } else {
      return new GridOptions(caption, filter, order);
    }
  }

  public static Filter getImmutableFilter(GridDescription gridDescription, GridOptions gridOptions) {
    Assert.notNull(gridDescription);
    if (gridOptions == null || !gridOptions.hasFilter()) {
      return gridDescription.getFilter();
    }

    return Filter.and(gridDescription.getFilter(), DataUtils.parseFilter(gridOptions.getFilter(),
        Data.getDataInfoProvider(), gridDescription.getViewName()));
  }

  public static Filter getInitialQueryFilter(Filter immutableFilter,
      Map<String, Filter> initialFilters) {

    List<Filter> filters = Lists.newArrayList();
    if (immutableFilter != null) {
      filters.add(immutableFilter);
    }

    if (initialFilters != null) {
      for (Filter filter : initialFilters.values()) {
        if (filter != null) {
          filters.add(filter);
        }
      }
    }
    return Filter.and(filters);
  }

  public static Order getOrder(GridDescription gridDescription, GridOptions gridOptions) {
    Assert.notNull(gridDescription);
    if (gridOptions == null || !gridOptions.hasOrder()) {
      return gridDescription.getOrder();
    }

    Order order = DataUtils.parseOrder(gridOptions.getOrder(), Data.getDataInfoProvider(),
        gridDescription.getViewName());
    if (order == null) {
      order = gridDescription.getOrder();
    }
    return order;
  }

  public static void openGrid(String gridName) {
    openGrid(gridName, getGridCallback(gridName), null);
  }

  public static void openGrid(String gridName, GridCallback gridCallback) {
    openGrid(gridName, gridCallback, null);
  }

  public static void openGrid(String gridName, GridCallback gridCallback, GridOptions gridOptions) {
    BeeKeeper.getScreen().updateActivePanel(ensureLoadingWidget());
    createGrid(gridName, gridCallback, EnumSet.of(UiOption.ROOT), gridOptions, SHOW);
  }

  public static void registerGridCallback(String gridName, GridCallback callback) {
    Assert.notEmpty(gridName);
    gridCallbacks.put(BeeUtils.normalize(gridName), callback);
  }

  public static void showGridInfo(String name) {
    if (descriptionCache.isEmpty()) {
      BeeKeeper.getLog().warning("grid description cache is empty");
      return;
    }

    if (!BeeUtils.isEmpty(name)) {
      if (isGridDescriptionCached(name)) {
        GridDescription gridDescription = descriptionCache.get(gridDescriptionKey(name));
        if (gridDescription != null) {
          BeeKeeper.getScreen().showGrid(gridDescription.getExtendedInfo());
          return;
        } else {
          BeeKeeper.getLog().warning("grid", name, "description was not found");
        }
      } else {
        BeeKeeper.getLog().warning("grid", name, "description not in cache");
      }
    }

    List<Property> info = Lists.newArrayList();
    for (Map.Entry<String, GridDescription> entry : descriptionCache.entrySet()) {
      GridDescription gridDescription = entry.getValue();
      String cc = (gridDescription == null) ? BeeConst.STRING_MINUS
          : BeeUtils.toString(gridDescription.getColumnCount());
      info.add(new Property(entry.getKey(), cc));
    }

    BeeKeeper.getScreen().showGrid(info, "Grid Name", "Column Count");
  }

  public static Widget simpleGrid(Object data, String... columnLabels) {
    Assert.notNull(data);

    IsTable<?, ?> table = createTable(data, columnLabels);
    Assert.notNull(table);

    int c = table.getNumberOfColumns();
    Assert.isPositive(c);

    int r = table.getNumberOfRows();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data table empty");
      return null;
    }

    CellGrid grid = new CellGrid();

    RowIdColumn idColumn = new RowIdColumn();
    String id = "row-id";
    grid.addColumn(id, -1, null, idColumn, new ColumnHeader(id, "Id", false));
    grid.setColumnWidth(id, 40);

    DataColumn<?> column;
    for (int i = 0; i < c; i++) {
      column = createColumn(table.getColumn(i), i);
      column.setSortable(true);
      column.setSortBy(Lists.newArrayList(table.getColumn(i).getId()));

      String label = table.getColumnLabel(i);
      grid.addColumn(label, i, null, column, new ColumnHeader(label, label, false));
    }

    grid.setReadOnly(true);

    grid.setHeaderCellHeight(23);
    grid.setBodyCellHeight(20);
    grid.estimateColumnWidths(table.getRows().getList(), 0, Math.min(r, 50));
    grid.estimateHeaderWidths(true);

    grid.setRowCount(r, false);
    grid.setRowData(table.getRows().getList(), true);

    return grid;
  }

  private static void createPresenter(GridDescription gridDescription, int rowCount,
      BeeRowSet rowSet, Provider.Type providerType, CachingPolicy cachingPolicy,
      Collection<UiOption> uiOptions, GridCallback gridCallback,
      Filter immutableFilter, Map<String, Filter> initialFilters,
      Order order, GridOptions gridOptions, PresenterCallback presenterCallback) {

    GridPresenter presenter = new GridPresenter(gridDescription, rowCount, rowSet, providerType,
        cachingPolicy, uiOptions, gridCallback, immutableFilter, initialFilters, order,
        gridOptions);

    if (gridCallback != null) {
      gridCallback.onShow(presenter);
    }

    presenterCallback.onCreate(presenter);
  }

  private static Widget ensureLoadingWidget() {
    if (loadingWidget == null) {
      loadingWidget = new BeeImage(Global.getImages().loading());
    }
    return loadingWidget;
  }

  private static void getInitialRowSet(final GridDescription gridDescription,
      final GridCallback gridCallback, final PresenterCallback presenterCallback,
      final Collection<UiOption> uiOptions, final GridOptions gridOptions) {

    final Filter immutableFilter = getImmutableFilter(gridDescription, gridOptions);
    final Map<String, Filter> initialFilters =
        (gridCallback == null) ? null : gridCallback.getInitialFilters();

    final Order order = getOrder(gridDescription, gridOptions);

    String viewName = gridDescription.getViewName();
    if (BeeUtils.isEmpty(viewName)) {
      BeeRowSet brs = null;
      if (gridCallback != null) {
        brs = gridCallback.getInitialRowSet();
      }

      if (brs == null) {
        BeeKeeper.getLog().severe("grid", gridDescription.getName(), "has no initial data");
      } else {
        createPresenter(gridDescription, brs.getNumberOfRows(), brs, Provider.Type.LOCAL,
            CachingPolicy.NONE, uiOptions, gridCallback, immutableFilter, initialFilters, order,
            gridOptions, presenterCallback);
      }
      return;
    }

    Filter queryFilter = getInitialQueryFilter(immutableFilter, initialFilters);

    int approximateRowCount = Data.getApproximateRowCount(viewName);

    int threshold;
    if (gridDescription.getAsyncThreshold() != null) {
      threshold = gridDescription.getAsyncThreshold();
    } else {
      threshold = DataUtils.getDefaultAsyncThreshold();
    }

    final Provider.Type providerType;
    final CachingPolicy cachingPolicy;

    if (threshold <= 0 || approximateRowCount > threshold) {
      providerType = Provider.Type.ASYNC;
      cachingPolicy = gridDescription.getCachingPolicy(true);
    } else {
      providerType = Provider.Type.CACHED;
      cachingPolicy = CachingPolicy.NONE;
    }

    int limit;
    if (Provider.Type.CACHED.equals(providerType)) {
      limit = BeeConst.UNDEF;
    } else if (gridDescription.getInitialRowSetSize() != null) {
      limit = gridDescription.getInitialRowSetSize();
    } else if (approximateRowCount >= 0
        && approximateRowCount <= DataUtils.getMaxInitialRowSetSize()) {
      limit = BeeConst.UNDEF;
    } else {
      limit = DataUtils.getMaxInitialRowSetSize();
    }
    
    final boolean requestSize = limit > 0;
    Collection<Property> queryOptions;
    if (requestSize) {
      queryOptions = PropertyUtils.createProperties(Service.VAR_VIEW_SIZE, requestSize);
    } else {
      queryOptions = null;
    }

    Queries.getRowSet(viewName, null, queryFilter, order, 0, limit, cachingPolicy, queryOptions,
        new Queries.RowSetCallback() {
          public void onSuccess(BeeRowSet rowSet) {
            Assert.notNull(rowSet);

            int rc = rowSet.getNumberOfRows();
            if (requestSize) {
              rc = Math.max(rc, BeeUtils.toInt(rowSet.getTableProperty(Service.VAR_VIEW_SIZE)));
            }

            createPresenter(gridDescription, rc, rowSet, providerType, cachingPolicy, uiOptions,
                gridCallback, immutableFilter, initialFilters, order, gridOptions,
                presenterCallback);
          }
        });
  }

  private static String gridDescriptionKey(String name) {
    return name.trim().toLowerCase();
  }

  private static boolean isGridDescriptionCached(String name) {
    if (BeeUtils.isEmpty(name)) {
      return false;
    }
    return descriptionCache.containsKey(gridDescriptionKey(name));
  }

  private GridFactory() {
  }
}