package com.butent.bee.client.grid;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.grid.cell.HtmlCell;
import com.butent.bee.client.grid.cell.TextCell;
import com.butent.bee.client.grid.column.AreaColumn;
import com.butent.bee.client.grid.column.BooleanColumn;
import com.butent.bee.client.grid.column.CurrencyColumn;
import com.butent.bee.client.grid.column.DataColumn;
import com.butent.bee.client.grid.column.DateColumn;
import com.butent.bee.client.grid.column.DateTimeColumn;
import com.butent.bee.client.grid.column.DecimalColumn;
import com.butent.bee.client.grid.column.DoubleColumn;
import com.butent.bee.client.grid.column.IntegerColumn;
import com.butent.bee.client.grid.column.LongColumn;
import com.butent.bee.client.grid.column.StringColumn;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.RenderableCell;
import com.butent.bee.client.render.RenderableColumn;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.ui.WidgetFactory;
import com.butent.bee.client.ui.WidgetSupplier;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridImpl;
import com.butent.bee.client.view.grid.ColumnInfo;
import com.butent.bee.client.view.grid.GridFilterManager;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridSettings;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.PropertiesData;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterComponent;
import com.butent.bee.shared.data.filter.FilterDescription;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.CellType;
import com.butent.bee.shared.ui.Flexibility;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public final class GridFactory {

  public static final class GridOptions {
    
    public static GridOptions forCurrentUserFilter(String column) {
      return BeeUtils.isEmpty(column) ? null : new GridOptions(null, null, column);
    }
    
    private final String caption;
    private final String filter;
    private final String currentUserFilter;

    private GridOptions(String caption, String filter, String currentUserFilter) {
      this.caption = caption;
      this.filter = filter;
      this.currentUserFilter = currentUserFilter;
    }
    
    public Filter buildFilter(String viewName) {
      Filter f1 = BeeUtils.isEmpty(getFilter()) ? null 
          : DataUtils.parseFilter(getFilter(), Data.getDataInfoProvider(), viewName);
      Filter f2 = BeeUtils.isEmpty(getCurrentUserFilter()) ? null 
          : BeeKeeper.getUser().getFilter(getCurrentUserFilter());
      
      return Filter.and(f1, f2);
    }

    public String getCaption() {
      return caption;
    }
    
    public String getCurrentUserFilter() {
      return currentUserFilter;
    }

    public String getFilter() {
      return filter;
    }

    public boolean hasFilter() {
      return !BeeUtils.isEmpty(getFilter()) || !BeeUtils.isEmpty(getCurrentUserFilter());
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(GridFactory.class);

  private static final Map<String, GridDescription> descriptionCache = Maps.newHashMap();
  private static final Map<String, GridInterceptor> gridInterceptors = Maps.newHashMap();
  
  private static final Multimap<String, String> hiddenColumns = HashMultimap.create();

  public static void clearDescriptionCache() {
    descriptionCache.clear();
  }

  public static AbstractCell<String> createCell(CellType cellType) {
    Assert.notNull(cellType);

    switch (cellType) {
      case HTML:
        return new HtmlCell();
      default:
        return new TextCell();
    }
  }

  public static DataColumn<?> createColumn(CellSource cellSource) {
    return createColumn(cellSource, null);
  }

  public static DataColumn<?> createColumn(CellSource cellSource, CellType cellType) {
    if (cellType != null) {
      return new StringColumn(createCell(cellType), cellSource);
    }

    ValueType type = cellSource.getValueType();
    if (type == null) {
      return new StringColumn(cellSource);
    }

    switch (type) {
      case BOOLEAN:
        return new BooleanColumn(cellSource);

      case DATE:
        return new DateColumn(cellSource);

      case DATE_TIME:
        return new DateTimeColumn(cellSource);

      case DECIMAL:
        if (cellSource.getScale() == 2) {
          return new CurrencyColumn(cellSource);
        } else {
          return new DecimalColumn(cellSource);
        }

      case INTEGER:
        return new IntegerColumn(cellSource);

      case LONG:
        return new LongColumn(cellSource);

      case NUMBER:
        return new DoubleColumn(cellSource);

      case TEXT:
        if (cellSource.isText()) {
          return new AreaColumn(cellSource);
        } else {
          return new StringColumn(cellSource);
        }

      case TIME_OF_DAY:
        return new StringColumn(cellSource);
    }

    Assert.untouchable();
    return null;
  }

  public static DataColumn<?> createColumn(CellSource cellSource, CellType cellType,
      AbstractCellRenderer renderer) {
    if (renderer == null) {
      return createColumn(cellSource, cellType);
    } else {
      return createRenderableColumn(renderer, cellSource, cellType);
    }
  }

  public static void createGrid(String gridName, final String supplierKey,
      final GridInterceptor gridInterceptor, final Collection<UiOption> uiOptions,
      final GridOptions gridOptions, final PresenterCallback presenterCallback) {

    Assert.notEmpty(gridName);
    Assert.notNull(presenterCallback);

    getGridDescription(gridName, new Callback<GridDescription>() {
      @Override
      public void onSuccess(GridDescription result) {
        Assert.notNull(result);
        if (gridInterceptor != null && !gridInterceptor.onLoad(result)) {
          return;
        }

        consumeGridDescription(GridSettings.apply(supplierKey, result), supplierKey,
            gridInterceptor, presenterCallback, uiOptions, gridOptions);
      }
    });
  }

  public static GridView createGridView(GridDescription gridDescription, String supplierKey,
      List<BeeColumn> dataColumns) {
    return createGridView(gridDescription, supplierKey, dataColumns, null,
        getGridInterceptor(gridDescription.getName()), null);
  }

  public static GridView createGridView(GridDescription gridDescription, String supplierKey,
      List<BeeColumn> dataColumns, String relColumn, GridInterceptor gridInterceptor, Order order) {

    GridView gridView = new GridImpl(gridDescription, supplierKey, dataColumns, relColumn,
        gridInterceptor);
    gridView.create(order);

    return gridView;
  }

  public static DataColumn<?> createRenderableColumn(AbstractCellRenderer renderer,
      CellSource cellSource, CellType cellType) {
    AbstractCell<String> cell = (cellType == null) ? new RenderableCell() : createCell(cellType);
    return new RenderableColumn(cell, cellSource, renderer);
  }

  public static void getGridDescription(String name, Callback<GridDescription> callback) {
    getGridDescription(name, callback, false);
  }

  public static void getGridDescription(final String name,
      final Callback<GridDescription> callback, boolean reload) {

    Assert.notEmpty(name);
    Assert.notNull(callback);

    if (!reload && isGridDescriptionCached(name)) {
      callback.onSuccess(descriptionCache.get(gridDescriptionKey(name)));
      return;
    }

    BeeKeeper.getRpc().sendText(Service.GET_GRID, name, new ResponseCallback() {
      @Override
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

  public static GridInterceptor getGridInterceptor(String gridName) {
    Assert.notEmpty(gridName);
    GridInterceptor interceptor = gridInterceptors.get(BeeUtils.normalize(gridName));
    if (interceptor != null) {
      GridInterceptor instance = interceptor.getInstance();
      if (instance != null) {
        return instance;
      }
    }
    return interceptor;
  }

  public static GridOptions getGridOptions(Map<String, String> attributes) {
    if (BeeUtils.isEmpty(attributes)) {
      return null;
    }

    String caption = attributes.get(UiConstants.ATTR_CAPTION);
    String filter = attributes.get(UiConstants.ATTR_FILTER);
    String currentUserFilter = attributes.get(UiConstants.ATTR_CURRENT_USER_FILTER);

    if (BeeUtils.allEmpty(caption, filter, currentUserFilter)) {
      return null;
    } else {
      return new GridOptions(Localized.maybeTranslate(caption), filter, currentUserFilter);
    }
  }

  public static Filter getImmutableFilter(GridDescription gridDescription,
      GridOptions gridOptions) {
    Assert.notNull(gridDescription);
    
    Filter f1 = gridDescription.getFilter();
    Filter f2 = BeeUtils.isEmpty(gridDescription.getCurrentUserFilter()) ? null 
        : BeeKeeper.getUser().getFilter(gridDescription.getCurrentUserFilter());
    
    if (gridOptions == null || !gridOptions.hasFilter()) {
      return Filter.and(f1, f2);
    } else {
      return Filter.and(f1, f2, gridOptions.buildFilter(gridDescription.getViewName()));
    }
  }

  public static Filter getInitialQueryFilter(Filter immutableFilter,
      Map<String, Filter> initialParentFilters, Filter initialUserFilter) {

    List<Filter> filters = Lists.newArrayList();
    if (immutableFilter != null) {
      filters.add(immutableFilter);
    }

    if (initialParentFilters != null) {
      for (Filter filter : initialParentFilters.values()) {
        if (filter != null) {
          filters.add(filter);
        }
      }
    }

    if (initialUserFilter != null) {
      filters.add(initialUserFilter);
    }

    return Filter.and(filters);
  }

  public static List<FilterDescription> getPredefinedFilters(GridDescription gridDescription,
      GridInterceptor gridInterceptor) {
    Assert.notNull(gridDescription);

    if (gridInterceptor == null) {
      return gridDescription.getPredefinedFilters();
    } else {
      return gridInterceptor.getPredefinedFilters(gridDescription.getPredefinedFilters());
    }
  }

  public static String getSupplierKey(String gridName, GridInterceptor gridInterceptor) {
    String key = (gridInterceptor == null) ? null : gridInterceptor.getSupplierKey();
    if (BeeUtils.isEmpty(key)) {
      Assert.notEmpty(gridName);
      key = "grid_" + BeeUtils.normalize(gridName);
    }
    return key;
  }
  
  public static void hideColumn(String gridName, String columnName) {
    Assert.notEmpty(gridName);
    Assert.notEmpty(columnName);
    
    hiddenColumns.put(gridName, columnName);
  }

  public static boolean isHidden(String gridName, String columnName) {
    return hiddenColumns.containsEntry(gridName, columnName);
  }
  
  public static void openGrid(String gridName) {
    openGrid(gridName, getGridInterceptor(gridName));
  }

  public static void openGrid(String gridName, GridInterceptor gridInterceptor) {
    openGrid(gridName, gridInterceptor, null);
  }

  public static void openGrid(String gridName, GridOptions gridOptions) {
    openGrid(gridName, getGridInterceptor(gridName), gridOptions);
  }

  public static void openGrid(String gridName, GridInterceptor gridInterceptor,
      GridOptions gridOptions) {
    openGrid(gridName, gridInterceptor, gridOptions, PresenterCallback.SHOW_IN_ACTIVE_PANEL);
  }

  public static void openGrid(final String gridName, final GridInterceptor gridInterceptor,
      final GridOptions gridOptions, PresenterCallback presenterCallback) {

    final String supplierKey = getSupplierKey(gridName, gridInterceptor);
    final Collection<UiOption> uiOptions = EnumSet.of(UiOption.ROOT);

    if (!WidgetFactory.hasSupplier(supplierKey)) {
      WidgetSupplier supplier = new WidgetSupplier() {
        @Override
        public void create(final Callback<IdentifiableWidget> callback) {
          createGrid(gridName, supplierKey, gridInterceptor, uiOptions, gridOptions,
              new PresenterCallback() {
                @Override
                public void onCreate(Presenter presenter) {
                  callback.onSuccess(presenter.getWidget());
                }
              });
        }
      };

      WidgetFactory.registerSupplier(supplierKey, supplier);
    }

    createGrid(gridName, supplierKey, gridInterceptor, uiOptions, gridOptions, presenterCallback);
  }

  public static void registerGridInterceptor(String gridName, GridInterceptor interceptor) {
    Assert.notEmpty(gridName);
    gridInterceptors.put(BeeUtils.normalize(gridName), interceptor);
  }

  public static void showGridInfo(String name) {
    if (descriptionCache.isEmpty()) {
      logger.warning("grid description cache is empty");
      return;
    }

    if (!BeeUtils.isEmpty(name)) {
      if (isGridDescriptionCached(name)) {
        GridDescription gridDescription = descriptionCache.get(gridDescriptionKey(name));
        if (gridDescription != null) {
          Global.showGrid(new ExtendedPropertiesData(gridDescription.getExtendedInfo(), true));
          return;
        } else {
          logger.warning("grid", name, "description was not found");
        }
      } else {
        logger.warning("grid", name, "description not in cache");
      }
    }

    List<Property> info = Lists.newArrayList();
    for (Map.Entry<String, GridDescription> entry : descriptionCache.entrySet()) {
      GridDescription gridDescription = entry.getValue();
      String cc = (gridDescription == null) ? BeeConst.STRING_MINUS
          : BeeUtils.toString(gridDescription.getColumnCount());
      info.add(new Property(entry.getKey(), cc));
    }

    Global.showGrid(new PropertiesData(info, "Grid Name", "Column Count"));
  }

  public static CellGrid simpleGrid(IsTable<?, ?> table, int containerWidth) {
    Assert.notNull(table);

    int c = table.getNumberOfColumns();
    Assert.isPositive(c);

    int r = table.getNumberOfRows();
    if (r <= 0) {
      logger.warning("data table empty");
      return null;
    }

    CellGrid grid = new CellGrid();

    DataColumn<?> column;
    for (int i = 0; i < c; i++) {
      CellSource source = CellSource.forColumn(table.getColumn(i), i);
      column = createColumn(source);

      String id = table.getColumnId(i);
      String label = table.getColumnLabel(i);

      ColumnInfo columnInfo = new ColumnInfo(id, label, source, column,
          new ColumnHeader(id, label));
      grid.addColumn(columnInfo);
    }

    grid.setReadOnly(true);

    grid.estimateHeaderWidths(true);
    grid.estimateColumnWidths(table.getRows().getList(), 0, Math.min(r, 50));

    grid.setDefaultFlexibility(new Flexibility(1, -1, true));
    int distrWidth = containerWidth;
    if (r > 10) {
      distrWidth -= DomUtils.getScrollBarWidth();
    }
    grid.doFlexLayout(distrWidth);

    grid.setRowCount(r, false);
    grid.setRowData(table.getRows().getList(), true);

    return grid;
  }

  private static void consumeGridDescription(final GridDescription gridDescription,
      String supplierKey, final GridInterceptor gridInterceptor,
      final PresenterCallback presenterCallback, final Collection<UiOption> uiOptions,
      final GridOptions gridOptions) {

    final Filter immutableFilter = getImmutableFilter(gridDescription, gridOptions);
    final Map<String, Filter> initialParentFilters =
        (gridInterceptor == null) ? null : gridInterceptor.getInitialParentFilters();

    List<FilterDescription> predefinedFilters =
        getPredefinedFilters(gridDescription, gridInterceptor);
    Global.getFilters().ensurePredefinedFilters(supplierKey, predefinedFilters);

    final List<FilterComponent> initialUserFilterValues =
        Global.getFilters().getInitialValues(supplierKey);

    final Order order = gridDescription.getOrder();

    String viewName = gridDescription.getViewName();

    BeeRowSet brs = null;
    if (gridInterceptor != null) {
      brs = gridInterceptor.getInitialRowSet(gridDescription);
    }

    if (BeeUtils.isEmpty(viewName) && brs == null) {
      logger.severe("grid", gridDescription.getName(), "has no initial data");
      return;
    }

    final Provider.Type providerType;
    final CachingPolicy cachingPolicy;

    final int approximateRowCount;

    if (BeeUtils.isEmpty(viewName)) {
      approximateRowCount = brs.getNumberOfRows();

      providerType = Provider.Type.LOCAL;
      cachingPolicy = CachingPolicy.NONE;

    } else {
      approximateRowCount = Data.getApproximateRowCount(viewName);

      int threshold;
      if (gridDescription.getAsyncThreshold() != null) {
        threshold = gridDescription.getAsyncThreshold();
      } else {
        threshold = DataUtils.getDefaultAsyncThreshold();
      }

      if (threshold <= 0 || approximateRowCount > threshold) {
        providerType = Provider.Type.ASYNC;
        cachingPolicy = gridDescription.getCachingPolicy(true);
      } else {
        providerType = Provider.Type.CACHED;
        cachingPolicy = CachingPolicy.NONE;
      }
    }

    if (brs != null) {
      GridView gridView = createGridView(gridDescription, supplierKey, brs.getColumns(),
          gridInterceptor, order);
      gridView.initData(brs.getNumberOfRows(), brs);

      Filter filter = GridFilterManager.parseFilter(gridView.getGrid(), initialUserFilterValues);

      createPresenter(gridDescription, gridView, brs.getNumberOfRows(), brs, providerType,
          cachingPolicy, uiOptions, gridInterceptor, immutableFilter, initialParentFilters,
          initialUserFilterValues, filter, order, gridOptions, presenterCallback);
      return;
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

    final GridView gridView = createGridView(gridDescription, supplierKey,
        Data.getColumns(viewName), gridInterceptor, order);

    final Filter initialUserFilter = GridFilterManager.parseFilter(gridView.getGrid(),
        initialUserFilterValues);
    Filter queryFilter = getInitialQueryFilter(immutableFilter, initialParentFilters,
        initialUserFilter);

    Queries.getRowSet(viewName, null, queryFilter, order, 0, limit, cachingPolicy, queryOptions,
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet rowSet) {
            Assert.notNull(rowSet);

            int rc = rowSet.getNumberOfRows();
            if (requestSize) {
              rc = Math.max(rc, BeeUtils.toInt(rowSet.getTableProperty(Service.VAR_VIEW_SIZE)));
            }

            gridView.initData(rc, rowSet);

            createPresenter(gridDescription, gridView, rc, rowSet, providerType, cachingPolicy,
                uiOptions, gridInterceptor, immutableFilter, initialParentFilters,
                initialUserFilterValues, initialUserFilter, order, gridOptions, presenterCallback);
          }
        });
  }

  private static GridView createGridView(GridDescription gridDescription, String supplierKey,
      List<BeeColumn> dataColumns, GridInterceptor gridInterceptor, Order order) {
    return createGridView(gridDescription, supplierKey, dataColumns, null, gridInterceptor, order);
  }

  private static void createPresenter(GridDescription gridDescription, GridView gridView,
      int rowCount, BeeRowSet rowSet, Provider.Type providerType, CachingPolicy cachingPolicy,
      Collection<UiOption> uiOptions, GridInterceptor gridInterceptor,
      Filter immutableFilter, Map<String, Filter> parentFilters,
      List<FilterComponent> userFilterValues, Filter userFilter,
      Order order, GridOptions gridOptions, PresenterCallback presenterCallback) {

    GridPresenter presenter = new GridPresenter(gridDescription, gridView, rowCount, rowSet,
        providerType, cachingPolicy, uiOptions, gridInterceptor, immutableFilter,
        parentFilters, userFilterValues, userFilter, order, gridOptions);

    if (gridInterceptor != null) {
      gridInterceptor.onShow(presenter);
    }

    presenterCallback.onCreate(presenter);
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