package com.butent.bee.client.data;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.BeeCellTable;
import com.butent.bee.client.grid.CellColumn;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.TableInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class AsyncProvider extends AbstractDataProvider<IsRow> {
  private class Callback implements ResponseCallback {
    private final HasData<IsRow> display;
    private final Range range;

    private Callback(HasData<IsRow> display, Range range) {
      this.display = display;
      this.range = range;
    }

    @Override
    public void onResponse(JsArrayString arr) {
      if (!display.getVisibleRange().equals(range)) {
        BeeKeeper.getLog().warning("range changed");
        return;
      }

      Assert.notNull(arr);
      BeeRowSet rs = BeeRowSet.restore(arr.get(0));
      if (rs.isEmpty()) {
        BeeKeeper.getLog().warning("rowset empty");
        return;
      }
      
      updateDisplay(rs);
    }

    private void updateDisplay(BeeRowSet data) {
      int start = range.getStart();
      int length = range.getLength();
      int rowCount = data.getNumberOfRows();
      
      BeeKeeper.getLog().info("upd", start, length, rowCount);
      
      Assert.nonNegative(start);
      Assert.isPositive(length);
      Assert.isPositive(rowCount);
      
      if (length == rowCount) {
        display.setRowData(start, data.getRows().getList());
      } else {
        display.setRowData(start, 
            data.getRows().getList().subList(0, BeeUtils.min(length, rowCount)));
      }
    }
  }

  private TableInfo tableInfo;
  private String where;
  private String order;

  public AsyncProvider(TableInfo tableInfo, String where, String order) {
    super();
    this.tableInfo = tableInfo;
    this.where = where;
    this.order = order;
  }

  public String getOrder() {
    return order;
  }

  public TableInfo getTableInfo() {
    return tableInfo;
  }

  public String getWhere() {
    return where;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public void setTableInfo(TableInfo tableInfo) {
    this.tableInfo = tableInfo;
  }

  public void setWhere(String where) {
    this.where = where;
  }

  @Override
  protected void onRangeChanged(HasData<IsRow> display) {
    Assert.notNull(display);
    Range range = display.getVisibleRange();

    List<Property> lst = PropertyUtils.createProperties("table_name", getTableInfo().getName(),
        "table_offset", range.getStart(), "table_limit", range.getLength());
    String wh = getWhere();
    if (!BeeUtils.isEmpty(wh)) {
      PropertyUtils.addProperties(lst, "table_where", wh);
    }
    
    String ord = null;
    if (display instanceof BeeCellTable) {
      ord = getTableOrder((BeeCellTable) display);
    }
    if (BeeUtils.isEmpty(ord)) {
      ord = getOrder();
    }
    if (!BeeUtils.isEmpty(ord)) {
      PropertyUtils.addProperties(lst, "table_order", ord);
    }

    BeeKeeper.getRpc().makeGetRequest(new ParameterList("rpc_ui_table", lst),
        new Callback(display, range));
  }
  
  private String getTableOrder(BeeCellTable grid) {
    ColumnSortList sortList = grid.getColumnSortList();
    if (sortList == null) {
      return null;
    }
    
    boolean hasId = false;
    String idLabel = getTableInfo().getIdColumn();

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < sortList.size(); i++) {
      ColumnSortInfo sortInfo = sortList.get(i);
      Column<?, ?> column = sortInfo.getColumn();
      if (!(column instanceof CellColumn)) {
        break;
      }
      String label = ((CellColumn<?>) column).getLabel();
      if (BeeUtils.isEmpty(label)) {
        break;
      }
      
      if (BeeUtils.same(label, idLabel)) {
        hasId = true;
      }
      if (sb.length() > 0) {
        sb.append(BeeConst.CHAR_SPACE);
      }
      sb.append(label.trim());
      if (!sortInfo.isAscending()) {
        sb.append(BeeConst.CHAR_MINUS);
      }
    }
    
    if (sb.length() <= 0) {
      return null;
    }
    if (!hasId && !BeeUtils.isEmpty(idLabel)) {
      sb.append(BeeConst.CHAR_SPACE).append(idLabel.trim());
    }
    return sb.toString();
  }
}
