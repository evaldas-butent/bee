package com.butent.bee.egg.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.grid.model.CachedTableModel;
import com.butent.bee.egg.client.grid.model.MutableTableModel;
import com.butent.bee.egg.client.grid.model.TableModel;
import com.butent.bee.egg.client.grid.model.TableModelHelper.Request;
import com.butent.bee.egg.client.grid.model.TableModelHelper.Response;
import com.butent.bee.egg.client.grid.render.DefaultRowRenderer;
import com.butent.bee.egg.client.grid.render.FixedWidthGridBulkRenderer;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.data.DataUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GridFactory {

  private class ScrollGridColumnDefinition extends ColumnDefinition<Integer, String> {
    private BeeView view;
    private int idx;
    private int maxDisplaySize;

    private ScrollGridColumnDefinition(BeeView view, int idx) {
      this(view, idx, -1);
    }

    private ScrollGridColumnDefinition(BeeView view, int idx, int max) {
      this.view = view;
      this.idx = idx;
      this.maxDisplaySize = max;
    }

    @Override
    public String getCellValue(Integer rowValue) {
      String v = view.getValue(rowValue, idx);
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

  private class ScrollGridTableModel extends MutableTableModel<Integer> {
    public void requestRows(Request request, TableModel.Callback<Integer> callback) {
      int start = request.getStartRow();
      int cnt = request.getNumRows();

      ScrollGridResponse resp = new ScrollGridResponse(start, cnt, getRowCount());
      callback.onRowsReady(request, resp);
    }

    protected boolean onRowInserted(int beforeRow) {
      return true;
    }

    protected boolean onRowRemoved(int row) {
      return true;
    }

    protected boolean onSetRowValue(int row, Integer rowValue) {
      return true;
    }
  }

  public Widget cellGrid(Object data, CellType cellType, Object... columns) {
    Assert.notNull(data);

    BeeView view = DataUtils.createView(data, columns);
    Assert.notNull(view);

    int c = view.getColumnCount();
    Assert.isPositive(c);

    int r = view.getRowCount();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data view empty");
      return null;
    }

    String table = null;
    CellKeyProvider keyProvider = null;

    if (!(view instanceof BeeRowSet) && BeeUtils.arrayLength(view.getColumns()) > 0) {
      table = view.getColumns()[0].getTable();
    }

    if (!BeeUtils.isEmpty(table)) {
      keyProvider = new CellKeyProvider(view);
      BeeGlobal.getCache().getPrimaryKey(table, keyProvider);
    }

    BeeCellTable cellTable = new BeeCellTable(r, keyProvider);

    TextColumn column;
    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      column = new TextColumn(createCell(cellType), view, i);
      if (cellType != null && cellType.isEditable()) {
        column.setFieldUpdater(new CellUpdater(view, i, keyProvider));
      }
      cellTable.addColumn(column, arr[i]);
    }
    cellTable.initData(r);

    return cellTable;
  }

  public Widget scrollGrid(Object data, Object... columns) {
    Assert.notNull(data);

    BeeView view = DataUtils.createView(data, columns);
    Assert.notNull(view);

    int c = view.getColumnCount();
    Assert.isPositive(c);

    int r = view.getRowCount();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data view empty");
      return null;
    }

    ScrollGridTableModel tableModel = new ScrollGridTableModel();
    CachedTableModel<Integer> cachedModel = new CachedTableModel<Integer>(tableModel);
    cachedModel.setRowCount(r);

    TableDefinition<Integer> tableDef = new TableDefinition<Integer>();
    String[] rowColors = new String[]{"#ffffdd", "#eeeeee"};
    tableDef.setRowRenderer(new DefaultRowRenderer<Integer>(rowColors));

    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      ScrollGridColumnDefinition colDef = new ScrollGridColumnDefinition(view, i, 512);
      colDef.setHeader(0, arr[i]);
      colDef.setFooter(0, "col " + i);

      tableDef.addColumnDefinition(colDef);
    }

    ScrollTable<Integer> table = new ScrollTable<Integer>(cachedModel, tableDef);

    FixedWidthGridBulkRenderer<Integer> renderer = new FixedWidthGridBulkRenderer<Integer>(
        table.getDataTable(), table);
    table.setBulkRenderer(renderer);

    return table;
  }

  public Widget simpleGrid(Object data, Object... columns) {
    Assert.notNull(data);

    BeeView view = DataUtils.createView(data, columns);
    Assert.notNull(view);

    int c = view.getColumnCount();
    Assert.isPositive(c);

    int r = view.getRowCount();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data view empty");
      return null;
    }

    BeeCellTable table = new BeeCellTable(r);

    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      table.addColumn(new TextColumn(createCell(CellType.TEXT), view, i, 256), arr[i]);
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