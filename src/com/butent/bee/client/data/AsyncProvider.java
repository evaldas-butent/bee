package com.butent.bee.client.data;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.grid.CellGrid;
import com.butent.bee.client.grid.CellColumn;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.ViewInfo;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.utils.BeeUtils;

public class AsyncProvider extends AbstractDataProvider<IsRow> {
  private class Callback implements Queries.RowSetCallback {
    private final HasData<IsRow> display;
    private final Range range;

    private Callback(HasData<IsRow> display, Range range) {
      this.display = display;
      this.range = range;
    }

    @Override
    public void onResponse(BeeRowSet rowSet) {
      if (!display.getVisibleRange().equals(range)) {
        BeeKeeper.getLog().warning("range changed");
        return;
      }

      if (rowSet.isEmpty()) {
        BeeKeeper.getLog().warning("rowset empty");
        return;
      }

      updateDisplay(rowSet);
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

  private ViewInfo viewInfo;
  private IsCondition where;
  private String order;

  public AsyncProvider(ViewInfo viewInfo, IsCondition where, String order) {
    super();
    this.viewInfo = viewInfo;
    this.where = where;
    this.order = order;
  }

  public String getOrder() {
    return order;
  }

  public ViewInfo getViewInfo() {
    return viewInfo;
  }

  public IsCondition getWhere() {
    return where;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public void setViewInfo(ViewInfo viewInfo) {
    this.viewInfo = viewInfo;
  }

  public void setWhere(IsCondition where) {
    this.where = where;
  }

  @Override
  protected void onRangeChanged(HasData<IsRow> display) {
    Assert.notNull(display);
    Range range = display.getVisibleRange();

    IsCondition condition = getWhere();

    String ord = null;
    if (display instanceof CellGrid) {
      ord = getViewOrder((CellGrid) display);
    }
    if (BeeUtils.isEmpty(ord)) {
      ord = getOrder();
    }
    
    Queries.getRowSet(getViewInfo().getName(), condition, ord, range.getStart(), range.getLength(),
        new Callback(display, range));
  }

  private String getViewOrder(CellGrid grid) {
    ColumnSortList sortList = grid.getColumnSortList();
    if (sortList == null) {
      return null;
    }

    boolean hasId = false;
    String idLabel = getViewInfo().getIdColumn();

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
