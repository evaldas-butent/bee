package com.butent.bee.client.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.CachedProvider;
import com.butent.bee.client.data.KeyProvider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.scrolltable.ColumnDefinition;
import com.butent.bee.client.grid.scrolltable.ScrollTable;
import com.butent.bee.client.grid.scrolltable.TableDefinition;
import com.butent.bee.client.grid.scrolltable.model.CachedTableModel;
import com.butent.bee.client.grid.scrolltable.model.TableModel;
import com.butent.bee.client.grid.scrolltable.model.TableModelHelper.Request;
import com.butent.bee.client.grid.scrolltable.model.TableModelHelper.Response;
import com.butent.bee.client.grid.scrolltable.render.FixedWidthGridBulkRenderer;
import com.butent.bee.client.presenter.GridPresenter;
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
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.CellType;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Creates simple and scroll grid objects for usage in the user interface.
 */

public class GridFactory {

  /**
   * Contains requirements for grid component callback methods.
   */

  public interface DescriptionCallback extends Callback<GridDescription, String[]> {
  }

  public interface PresenterCallback {
    void onCreate(GridPresenter presenter);
  }

  /**
   * Sets id and order for scroll grid columns.
   */

  private static class ScrollGridColumnDefinition extends ColumnDefinition {
    private int idx;
    private int maxDisplaySize;

    private ScrollGridColumnDefinition(int idx) {
      this(idx, -1);
    }

    private ScrollGridColumnDefinition(int idx, int max) {
      this.idx = idx;
      this.maxDisplaySize = max;

      setColumnId(idx);
      setColumnOrder(idx);
    }

    @Override
    public String getCellValue(IsRow rowValue) {
      String v = rowValue.getString(idx);
      if (v == null) {
        return BeeConst.STRING_EMPTY;
      }
      if (maxDisplaySize <= 0 || v.length() <= maxDisplaySize) {
        return v;
      }

      return BeeUtils.clip(v, maxDisplaySize);
    }

    @Override
    public void setCellValue(IsRow rowValue, Object cellValue) {
      rowValue.setValue(idx, BeeUtils.transform(cellValue));
    }
  }

  /**
   * Adds up row values into one object for response purposes.
   */

  private static class ScrollGridResponse extends Response {
    private Collection<IsRow> rowValues = new ArrayList<IsRow>();

    private ScrollGridResponse(IsTable<?, ?> data, int start, int cnt, int tot) {
      for (int i = start; i < start + cnt && i < tot; i++) {
        this.rowValues.add(data.getRow(i));
      }
    }

    @Override
    public Iterator<IsRow> getRowValues() {
      return rowValues.iterator();
    }
  }

  /**
   * Extends {@code TableModel} class, gets starting row of selection and number of rows to select.
   */

  private static class ScrollGridTableModel extends TableModel {
    private IsTable<?, ?> data;

    private ScrollGridTableModel(IsTable<?, ?> data) {
      super();
      this.data = data;
    }

    public void requestRows(Request request, Callback callback) {
      int start = request.getStartRow();
      int cnt = request.getNumRows();

      ScrollGridResponse resp = new ScrollGridResponse(data, start, cnt, data.getNumberOfRows());
      callback.onRowsReady(request, resp);
    }
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

  public static Widget cellTable(Object data, TextCellType cellType, String... columnLabels) {
    Assert.notNull(data);

    IsTable<?, ?> table = DataUtils.createTable(data, columnLabels);
    Assert.notNull(table);

    int c = table.getNumberOfColumns();
    Assert.isPositive(c);

    int r = table.getNumberOfRows();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data table empty");
      return null;
    }

    CellTable<IsRow> grid = new CellTable<IsRow>(r);

    TextColumn column;
    for (int i = 0; i < c; i++) {
      column = new TextColumn(createCell(cellType), i, table.getColumn(i));
      if (cellType != null && cellType.isEditable()) {
        column.setFieldUpdater(new CellUpdater(i));
      }
      grid.addColumn(column, table.getColumnLabel(i));
    }

    MultiSelectionModel<IsRow> selector = new MultiSelectionModel<IsRow>(new KeyProvider());
    grid.setSelectionModel(selector);

    grid.setRowCount(r, true);
    grid.setRowData(table.getRows().getList());

    return grid;
  }

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

  public static void createGrid(String gridName, final GridCallback gridCallback,
      final PresenterCallback presenterCallback, final Collection<UiOption> options) {
    Assert.notEmpty(gridName);
    Assert.notNull(presenterCallback);

    getGrid(gridName, new DescriptionCallback() {
      public void onFailure(String[] reason) {
        BeeKeeper.getScreen().notifySevere(reason);
      }

      public void onSuccess(GridDescription result) {
        Assert.notNull(result);
        if (gridCallback != null && !gridCallback.onLoad(result)) {
          return;
        }
        getInitialRowSet(result, gridCallback, presenterCallback, options);
      }
    });
  }

  public static void createGrid(String gridName, PresenterCallback presenterCallback,
      Collection<UiOption> options) {
    createGrid(gridName, getGridCallback(gridName), presenterCallback, options);
  }

  public static void getGrid(String name, DescriptionCallback callback) {
    getGrid(name, callback, false);
  }

  public static void getGrid(final String name, final DescriptionCallback callback,
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
          descriptionCache.put(gridDescriptionKey(name), gridDescription);
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

  public static void openGrid(String gridName) {
    openGrid(gridName, getGridCallback(gridName));
  }

  public static void openGrid(String gridName, GridCallback gridCallback) {
    BeeKeeper.getScreen().updateActivePanel(ensureLoadingWidget());
    createGrid(gridName, gridCallback, SHOW, EnumSet.of(UiOption.ROOT));
  }

  public static void registerGridCallback(String gridName, GridCallback callback) {
    Assert.notEmpty(gridName);
    gridCallbacks.put(BeeUtils.normalize(gridName), callback);
  }

  public static Widget scrollGrid(int width, Object data, String... columnLabels) {
    Assert.notNull(data);

    IsTable<?, ?> table = DataUtils.createTable(data, columnLabels);
    Assert.notNull(table);

    int c = table.getNumberOfColumns();
    Assert.isPositive(c);

    int r = table.getNumberOfRows();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data table empty");
      return null;
    }

    ScrollGridTableModel tableModel = new ScrollGridTableModel(table);
    CachedTableModel cachedModel = new CachedTableModel(tableModel);
    cachedModel.setRowCount(r);

    TableDefinition tableDef = new TableDefinition();

    for (int i = 0; i < c; i++) {
      ScrollGridColumnDefinition colDef = new ScrollGridColumnDefinition(i, 512);
      colDef.setHeader(table.getColumnLabel(i));
      colDef.setFooter("col " + i);

      tableDef.addColumnDefinition(colDef);
    }

    ScrollTable grid = new ScrollTable(cachedModel, tableDef);
    if (width > c) {
      int w = (width - DomUtils.getScrollbarWidth() - 2) / c;
      grid.setDefaultColumnWidth(BeeUtils.limit(w, 60, 300));
    }
    grid.createFooterTable();

    FixedWidthGridBulkRenderer renderer =
        new FixedWidthGridBulkRenderer(grid.getDataTable(), grid);
    grid.setBulkRenderer(renderer);

    return grid;
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
          BeeKeeper.getScreen().showGrid(gridDescription.getInfo());
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

    IsTable<?, ?> table = DataUtils.createTable(data, columnLabels);
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

    @SuppressWarnings("unused")
    CachedProvider provider = new CachedProvider(grid, table);

    grid.setReadOnly(true);

    grid.setHeaderCellHeight(23);
    grid.setBodyCellHeight(20);
    grid.estimateColumnWidths(table.getRows().getList(), Math.min(r, 20));
    grid.estimateHeaderWidths(true);

    grid.setRowCount(r, false);
    grid.setRowData(table.getRows().getList(), true);

    return grid;
  }

  private static Cell<String> createCell(TextCellType type) {
    Cell<String> cell;

    if (type == null) {
      cell = new TextCell();
    } else {
      switch (type) {
        case TEXT_EDIT:
          cell = new EditTextCell();
          break;
        case TEXT_INPUT:
          cell = new TextInputCell();
          break;
        default:
          cell = new TextCell();
      }
    }
    return cell;
  }

  private static void createPresenter(int rowCount, BeeRowSet rowSet, boolean async,
      GridDescription gridDescription, GridCallback gridCallback,
      PresenterCallback presenterCallback, Collection<UiOption> options) {
    GridPresenter presenter = new GridPresenter(gridDescription.getViewName(), rowCount, rowSet,
        async, gridDescription, gridCallback, options);
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
      final Collection<UiOption> options) {
    final int threshold = BeeUtils.unbox(gridDescription.getAsyncThreshold());
    int limit = BeeUtils.unbox(gridDescription.getInitialRowSetSize());
    if (limit <= 0) {
      limit = DataUtils.getMaxInitialRowSetSize();
    }

    Queries.getRowSet(gridDescription.getViewName(), null, gridDescription.getInitialFilter(),
        gridDescription.getOrder(), 0, limit, gridDescription.getCachingPolicy(),
        PropertyUtils.createProperties(Service.VAR_VIEW_SIZE, threshold),
        new Queries.RowSetCallback() {
          public void onFailure(String[] reason) {
          }

          public void onSuccess(final BeeRowSet rowSet) {
            Assert.notNull(rowSet);

            int rc = BeeUtils.toInt(rowSet.getTableProperty(Service.VAR_VIEW_SIZE));
            boolean async = threshold <= 0 || rc >= threshold || rc != rowSet.getNumberOfRows();
            rc = Math.max(rc, rowSet.getNumberOfRows());

            createPresenter(rc, rowSet, async, gridDescription, gridCallback, presenterCallback,
                options);
          }
        });
  }

  private static String gridDescriptionKey(String name) {
    Assert.notEmpty(name);
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