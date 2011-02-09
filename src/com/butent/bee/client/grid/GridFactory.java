package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.model.CachedTableModel;
import com.butent.bee.client.grid.model.TableModel;
import com.butent.bee.client.grid.model.TableModelHelper.Request;
import com.butent.bee.client.grid.model.TableModelHelper.Response;
import com.butent.bee.client.grid.render.FixedWidthGridBulkRenderer;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GridFactory {

  private class ScrollGridColumnDefinition extends ColumnDefinition<Integer, String> {
    private IsTable<?, ?> view;
    private int idx;
    private int maxDisplaySize;

    private ScrollGridColumnDefinition(IsTable<?, ?> view, int idx) {
      this(view, idx, -1);
    }

    private ScrollGridColumnDefinition(IsTable<?, ?> view, int idx, int max) {
      this.view = view;
      this.idx = idx;
      this.maxDisplaySize = max;
      
      setColumnId(idx);
      setColumnOrder(idx);
    }

    @Override
    public String getCellValue(Integer rowValue) {
      String v = view.getString(rowValue, idx);
      if (v == null) {
        return BeeConst.STRING_EMPTY;
      }
      if (maxDisplaySize <= 0 || v.length() <= maxDisplaySize) {
        return v;
      }

      return BeeUtils.clip(v, maxDisplaySize);
    }

    @Override
    public void setCellValue(Integer rowValue, String cellValue) {
      view.setValue(rowValue, idx, cellValue);
    }
  }

  private class ScrollGridResponse extends Response<Integer> {
    private Collection<Integer> rowValues = new ArrayList<Integer>();

    private ScrollGridResponse(int start, int cnt, int tot) {
      for (int i = start; i < start + cnt && i < tot; i++) {
        this.rowValues.add(i);
      }
    }

    @Override
    public Iterator<Integer> getRowValues() {
      return rowValues.iterator();
    }
  }

  private class ScrollGridTableModel extends TableModel<Integer> {
    public void requestRows(Request request, TableModel.Callback<Integer> callback) {
      int start = request.getStartRow();
      int cnt = request.getNumRows();

      ScrollGridResponse resp = new ScrollGridResponse(start, cnt, getRowCount());
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

    BeeCellTable cellTable = new BeeCellTable(r);

    TextColumn column;
    for (int i = 0; i < c; i++) {
      column = new TextColumn(createCell(cellType), table, i);
      if (cellType != null && cellType.isEditable()) {
        column.setFieldUpdater(new CellUpdater(table, i));
      }
      cellTable.addColumn(column, table.getColumnLabel(i));
    }
    cellTable.initData(r);

    return cellTable;
  }

  public Widget scrollGrid(int width, Object data, String... columnLabels) {
    Assert.notNull(data);

    IsTable<?, ?> view = DataUtils.createTable(data, columnLabels);
    Assert.notNull(view);

    int c = view.getNumberOfColumns();
    Assert.isPositive(c);

    int r = view.getNumberOfRows();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data table empty");
      return null;
    }

    ScrollGridTableModel tableModel = new ScrollGridTableModel();
    CachedTableModel<Integer> cachedModel = new CachedTableModel<Integer>(tableModel);
    cachedModel.setRowCount(r);

    TableDefinition<Integer> tableDef = new TableDefinition<Integer>();

    for (int i = 0; i < c; i++) {
      ScrollGridColumnDefinition colDef = new ScrollGridColumnDefinition(view, i, 512);
      colDef.setHeader(view.getColumnLabel(i));
      colDef.setFooter("col " + i);

      tableDef.addColumnDefinition(colDef);
    }

    ScrollTable<Integer> table = new ScrollTable<Integer>(cachedModel, tableDef);
    if (width > c) {
      int w = (width - DomUtils.getScrollbarWidth() - 2) / c;
      table.setDefaultColumnWidth(BeeUtils.limit(w, 60, 300));
    }
    table.createFooterTable();

    FixedWidthGridBulkRenderer<Integer> renderer = 
      new FixedWidthGridBulkRenderer<Integer>(table.getDataTable(), table);
    table.setBulkRenderer(renderer);

    return table;
  }

  public Widget simpleGrid(Object data, String... columnLabels) {
    Assert.notNull(data);

    IsTable<?, ?> view = DataUtils.createTable(data, columnLabels);
    Assert.notNull(view);

    int c = view.getNumberOfColumns();
    Assert.isPositive(c);

    int r = view.getNumberOfRows();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data table empty");
      return null;
    }

    BeeCellTable table = new BeeCellTable(r);

    for (int i = 0; i < c; i++) {
      table.addColumn(new TextColumn(createCell(CellType.TEXT), view, i, 256),
          view.getColumnLabel(i));
    }
    table.initData(r);

    return table;
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
}