package com.butent.bee.client.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.KeyProvider;
import com.butent.bee.client.data.DataProvider;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.model.CachedTableModel;
import com.butent.bee.client.grid.model.TableModel;
import com.butent.bee.client.grid.model.TableModelHelper.Request;
import com.butent.bee.client.grid.model.TableModelHelper.Response;
import com.butent.bee.client.grid.render.FixedWidthGridBulkRenderer;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GridFactory {

  private class ScrollGridColumnDefinition extends ColumnDefinition {
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

  private class ScrollGridResponse extends Response {
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

  private class ScrollGridTableModel extends TableModel {
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

  public Widget cellGrid(Object data, CellType cellType, String... columnLabels) {
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

    BeeCellTable grid = new BeeCellTable(r);

    TextColumn column;
    for (int i = 0; i < c; i++) {
      column = new TextColumn(createCell(cellType), i);
      if (cellType != null && cellType.isEditable()) {
        column.setFieldUpdater(new CellUpdater(i));
      }
      grid.addColumn(column, table.getColumnLabel(i));
    }
    grid.initData(table);

    return grid;
  }

  public Widget scrollGrid(int width, Object data, String... columnLabels) {
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

  public Widget simpleGrid(Object data, String... columnLabels) {
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
    
    Map<Integer, Integer> colWidths = calculateColumnWidths(table);
    boolean fixedLayout = colWidths.size() == c;

    BeeCellTable grid = new BeeCellTable(r);
    if (fixedLayout) {
      int gridWidth = 0;
      for (int i = 0; i < c; i++) {
        gridWidth += colWidths.get(i);
      }
      grid.setWidth(gridWidth + Unit.EM.getType(), true);
    }

    CellColumn<?> column;
    for (int i = 0; i < c; i++) {
      column = createColumn(table.getColumn(i), i);
      column.setSortable(true);
      if (ValueType.isNumber(table.getColumnType(i))) {
        column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LOCALE_END);
      }
      grid.addColumn(column, table.getColumnLabel(i));
      if (fixedLayout) {
        grid.setColumnWidth(column, colWidths.get(i), Unit.EM);
      }
    }

    DataProvider provider = new DataProvider(table);
    provider.addDataDisplay(grid);

    TableSorter sorter = new TableSorter(provider);
    grid.addColumnSortHandler(sorter);
    
    final MultiSelectionModel<IsRow> selector = new MultiSelectionModel<IsRow>(new KeyProvider());
    selector.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        List<Long> indexes = Lists.newArrayList();
        for (IsRow row : selector.getSelectedSet()) {
          indexes.add(row.getId());
        }
        BeeKeeper.getLog().info(indexes);
      }
    });
    grid.setSelectionModel(selector);

    return grid;
  }

  private Map<Integer, Integer> calculateColumnWidths(IsTable<?, ?> table) {
    Map<Integer, Integer> colWidths = Maps.newHashMap();
    if (table == null) {
      return colWidths;
    }
    int cc = table.getNumberOfColumns();
    if (cc <= 0) {
      return colWidths;
    }
    
    int w;
    for (int i = 0; i < cc; i++) {
      IsColumn column = table.getColumn(i);
      if (column instanceof BeeColumn) {
        w = getColumnWidth((BeeColumn) column);
      } else {
        w = -1;
      }
      
      if (w > 0) {
        colWidths.put(i, BeeUtils.limit(w, 3, 30));
      } else {
        colWidths.clear();
        break;
      }
    }
    return colWidths;
  }
  
  private Cell<String> createCell(CellType type) {
    Cell<String> cell;

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
    return cell;
  }
  
  private CellColumn<?> createColumn(IsColumn dataColumn, int index) {
    ValueType type = dataColumn.getType();
    if (type == null) {
      return new TextColumn(index);
    }

    switch (type) {
      case BOOLEAN:
        return new BooleanColumn(index);
      case DATE:
        return new DateColumn(index);
      case DATETIME:
        return new DateTimeColumn(index);
      case NUMBER:
        if (dataColumn instanceof BeeColumn) {
          if (((BeeColumn) dataColumn).getScale() == 2) {
            return new NumberColumn(NumberFormat.getCurrencyFormat(), index);
          }
          switch (((BeeColumn) dataColumn).getSqlType()) {
            case 4:
              return new NumberColumn(NumberFormat.getFormat("#"), index);
            case 6:
            case 7:
            case 8:
              return new NumberColumn(NumberFormat.getFormat("#.#######"), index);
          }
        }
        return new NumberColumn(index);
      default:
        return new TextColumn(index);
    }
  }
  
  private int getColumnWidth(BeeColumn column) {
    ValueType type = column.getType();
    int size = column.getPrecision();
    if (type == null) {
      return size;
    }

    switch (type) {
      case BOOLEAN:
        return 5;
      case DATE:
        return 10;
      case DATETIME:
        return 16;
      case NUMBER:
        switch (column.getSqlType()) {
          case 4:
            return 10;
          case 6:
          case 7:
          case 8:
            return 16;
          default:
            return size;
        }
      default:
        return size;
    }
  }
}