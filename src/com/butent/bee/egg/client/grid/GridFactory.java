package com.butent.bee.egg.client.grid;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.pst.AbstractColumnDefinition;
import com.butent.bee.egg.client.pst.AbstractScrollTable.ColumnResizePolicy;
import com.butent.bee.egg.client.pst.AbstractScrollTable.ResizePolicy;
import com.butent.bee.egg.client.pst.CachedTableModel;
import com.butent.bee.egg.client.pst.DefaultRowRenderer;
import com.butent.bee.egg.client.pst.DefaultTableDefinition;
import com.butent.bee.egg.client.pst.FixedWidthFlexTable;
import com.butent.bee.egg.client.pst.FixedWidthGrid;
import com.butent.bee.egg.client.pst.FixedWidthGridBulkRenderer;
import com.butent.bee.egg.client.pst.MutableTableModel;
import com.butent.bee.egg.client.pst.PagingScrollTable;
import com.butent.bee.egg.client.pst.ScrollTable;
import com.butent.bee.egg.client.pst.TableModel;
import com.butent.bee.egg.client.pst.TableModelHelper.Request;
import com.butent.bee.egg.client.pst.TableModelHelper.Response;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.data.DataUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GridFactory {

  private class PstColumnDefinition extends
      AbstractColumnDefinition<Integer, String> {
    private BeeView view;
    private int idx;
    private int maxDisplaySize;

    private PstColumnDefinition(BeeView view, int idx) {
      this(view, idx, -1);
    }

    private PstColumnDefinition(BeeView view, int idx, int max) {
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

  private class PstResponse extends Response<Integer> {
    private Collection<Integer> rowValues = new ArrayList<Integer>();

    private PstResponse(int start, int cnt, int tot) {
      for (int i = start; i < start + cnt && i < tot; i++) {
        this.rowValues.add(i);
      }
    }

    @Override
    public Iterator<Integer> getRowValues() {
      return rowValues.iterator();
    }
  }

  private class PstTableModel extends MutableTableModel<Integer> {
    public void requestRows(Request request, TableModel.Callback<Integer> callback) {
      int start = request.getStartRow();
      int cnt = request.getNumRows();
      
      PstResponse resp = new PstResponse(start, cnt, getRowCount()); 
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

  public Widget pstGrid(Object data, Object... columns) {
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

    PstTableModel tableModel = new PstTableModel();
    CachedTableModel<Integer> cachedTableModel = new CachedTableModel<Integer>(
        tableModel);
    cachedTableModel.setRowCount(r);

    DefaultTableDefinition<Integer> tableDef = new DefaultTableDefinition<Integer>();
    String[] rowColors = new String[] {"#ffffdd", "#eeeeee"};
    tableDef.setRowRenderer(new DefaultRowRenderer<Integer>(rowColors));

    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      PstColumnDefinition colDef = new PstColumnDefinition(view, i, 512);
      colDef.setHeader(0, arr[i]);
      colDef.setFooter(0, "col " + i);

      colDef.setMinimumColumnWidth(60);
      colDef.setMaximumColumnWidth(200);

      tableDef.addColumnDefinition(colDef);
    }

    PagingScrollTable<Integer> table = new PagingScrollTable<Integer>(
        cachedTableModel, tableDef);
    
    FixedWidthGridBulkRenderer<Integer> bulkRenderer = new FixedWidthGridBulkRenderer<Integer>(
        table.getDataTable(), table);
    table.setBulkRenderer(bulkRenderer);

    table.setCellPadding(3);
    table.setCellSpacing(0);

    table.setResizePolicy(ResizePolicy.UNCONSTRAINED);
    table.setColumnResizePolicy(ColumnResizePolicy.SINGLE_CELL);
    
    table.setPageSize(r);
    table.gotoFirstPage();

    return table;
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

    FixedWidthFlexTable headerTable = new FixedWidthFlexTable();
    FixedWidthFlexTable footerTable = new FixedWidthFlexTable();
    FixedWidthGrid dataTable = new FixedWidthGrid(r, c);

    ScrollTable table = new ScrollTable(dataTable, headerTable);
    table.setFooterTable(footerTable);

    table.setCellPadding(3);
    table.setCellSpacing(0);
    table.setResizePolicy(ResizePolicy.FLOW);

    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      headerTable.setHTML(0, i, arr[i]);
      footerTable.setHTML(0, i, "col " + i);
    }

    for (int i = 0; i < r; i++) {
      for (int j = 0; j < c; j++) {
        dataTable.setHTML(i, j, view.getValue(i, j));
      }
    }

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
      table.addColumn(new BeeTextColumn(view, i, 256), arr[i]);
    }
    table.initData(r);

    return table;
  }

}