package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.view.client.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Extends {@code Provider} class, enables to manage data ranges from sources stored directly in
 * memory.
 */

public class CachedProvider extends Provider {

  private final String viewName;
  private IsTable<?, ?> table;

  private final Set<Long> filteredRowIds = Sets.newHashSet();
  private final List<IsRow> viewRows = Lists.newArrayList();

  public CachedProvider(HasDataTable display, String viewName, IsTable<?, ?> table) {
    super(display);
    Assert.notNull(table);
    this.viewName = viewName;
    this.table = table;
  }

  public int getRowCount() {
    if (getViewRows().isEmpty()) {
      return table.getNumberOfRows();
    } else {
      return getViewRows().size();
    }
  }

  public IsTable<?, ?> getTable() {
    return table;
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      long id = event.getRowId();
      for (IsRow row : getTable().getRows()) {
        if (row.getId() == id) {
          row.setVersion(event.getVersion());
          row.setValue(getTable().getColumnIndex(event.getColumnId()), event.getValue());
          break;
        }
      }
      super.onCellUpdate(event);
    }
  }

  @Override
  public void onFilterChanged(Filter newFilter, int rowCount) {
    applyFilter(newFilter);

    if (newFilter != null) {
      int cnt = getFilteredRowIds().size();
      if (cnt != rowCount) {
        BeeKeeper.getLog().severe("row count", rowCount, "fitered rows", cnt);
      }
    }
    super.onFilterChanged(newFilter, rowCount);
  }
  
  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      for (RowInfo rowInfo : event.getRows()) {
        deleteRow(rowInfo.getId());
      }
      super.onMultiDelete(event);
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      deleteRow(event.getRowId());
      super.onRowDelete(event);
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      IsRow newRow = event.getRow();
      long id = newRow.getId();
      
      for (IsRow oldRow : getTable().getRows()) {
        if (oldRow.getId() == id) {
          oldRow.setVersion(newRow.getVersion());
          for (int i = 0; i < getTable().getNumberOfColumns(); i++) {
            oldRow.setValue(i, newRow.getString(i));
          }
          break;
        }
      }
      super.onRowUpdate(event);
    }
  }
  
  @Override
  public void onSort(SortEvent event) {
    if (getTable().getNumberOfRows() <= 1) {
      return;
    }
    Order order = event.getOrder();

    if (order == null || order.getSize() <= 0) {
      getTable().sortByRowId(true);
    } else {
      List<Pair<Integer, Boolean>> sortList = Lists.newArrayList();

      for (Order.Column sortInfo : order.getColumns()) {
        int index = getTable().getColumnIndex(sortInfo.getName());
        if (index < 0 || index > getTable().getNumberOfColumns()) {
          BeeKeeper.getLog().warning("onSort: column id", sortInfo.getName(), "not found");
        }
        sortList.add(new Pair<Integer, Boolean>(index, sortInfo.isAscending()));
      }
      if (!sortList.isEmpty()) {
        getTable().sort(sortList);
      }
    }

    if (getViewRows().size() > 1) {
      updateViewRows();
    }
    goTop();
  }

  @Override
  public void refresh() {
    if (BeeUtils.isEmpty(getViewName())) {
      BeeKeeper.getLog().warning("refresh: view name not available");
      return;
    }
    super.refresh();
  }

  public void updateDisplay(boolean updateActiveRow) {
    Range range = getRange();
    int start = range.getStart();
    int length = range.getLength();
    int rowCount = getRowCount();

    List<? extends IsRow> rowValues;
    if (start == 0 && length >= rowCount) {
      rowValues = getRowList();
    } else {
      rowValues = getRowList().subList(start, BeeUtils.min(start + length, rowCount));
    }

    if (updateActiveRow) {
      getDisplay().updateActiveRow(rowValues);
    }
    getDisplay().setRowData(start, rowValues);
  }

  protected String getViewName() {
    return viewName;
  }
  
  @Override
  protected void onRangeChanged(boolean updateActiveRow) {
    updateDisplay(updateActiveRow);
  }

  @Override
  protected void onRefresh() {
    String name = getViewName();
    if (BeeUtils.isEmpty(name)) {
      return;
    }
    
    final int oldPageSize = getPageSize();
    final int oldTableSize = getTable().getNumberOfRows();

    Queries.getRowSet(name, getOrder(), new Queries.RowSetCallback() {
      public void onFailure(String reason) {
      }

      public void onSuccess(BeeRowSet rowSet) {
        Assert.notNull(rowSet);
        
        setTable(rowSet);
        applyFilter(getFilter());
        
        int newTableSize = rowSet.getNumberOfRows();
        if (oldPageSize > 0 && oldPageSize >= oldTableSize && newTableSize != oldTableSize) {
          getDisplay().setPageSize(newTableSize);
        }
        
        updateDisplay(true);
      }
    });
  }

  private void applyFilter(Filter newFilter) {
    getFilteredRowIds().clear();
    getViewRows().clear();

    if (newFilter != null) {
      List<? extends IsColumn> columns = getTable().getColumns();
      for (IsRow row : getTable().getRows()) {
        if (newFilter.isMatch(columns, row)) {
          filteredRowIds.add(row.getId());
          viewRows.add(row);
        }
      }
    }
  }
  
  private void deleteRow(long rowId) {
    getTable().removeRowById(rowId);
    if (filteredRowIds.contains(rowId)) {
      filteredRowIds.remove(rowId);
      for (Iterator<IsRow> it = viewRows.iterator(); it.hasNext(); ) {
        if (it.next().getId() == rowId) {
          it.remove();
          break;
        }
      }
    }
  }
  
  private Set<Long> getFilteredRowIds() {
    return filteredRowIds;
  }

  private List<? extends IsRow> getRowList() {
    if (getViewRows().isEmpty()) {
      return getTable().getRows().getList();
    } else {
      return getViewRows();
    }
  }

  private List<IsRow> getViewRows() {
    return viewRows;
  }

  private void setTable(IsTable<?, ?> table) {
    this.table = table;
  }

  private void updateViewRows() {
    getViewRows().clear();
    if (getFilteredRowIds().isEmpty()) {
      return;
    }

    for (IsRow row : getTable().getRows()) {
      if (filteredRowIds.contains(row.getId())) {
        viewRows.add(row);
      }
    }
  }
}
