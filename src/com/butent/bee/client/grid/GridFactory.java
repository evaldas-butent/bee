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
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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

    BeeCellTable grid = new BeeCellTable(r);
    for (int i = 0; i < c; i++) {
      grid.addColumn(new TextColumn(createCell(CellType.TEXT), i, 256), table.getColumnLabel(i));
    }
    grid.initData(table);

    return grid;
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