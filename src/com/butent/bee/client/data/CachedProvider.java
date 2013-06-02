package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.client.event.logical.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.NavigationOrigin;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

/**
 * Extends {@code Provider} class, enables to manage data ranges from sources stored directly in
 * memory.
 */

public class CachedProvider extends Provider {

  private static final BeeLogger logger = LogUtils.getLogger(CachedProvider.class);

  private BeeRowSet table;

  private final Set<Long> filteredRowIds = Sets.newHashSet();
  private final List<BeeRow> viewRows = Lists.newArrayList();

  public CachedProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, BeeRowSet table) {
    this(display, notificationListener, viewName, columns, null, null, null, table);
  }

  public CachedProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, Filter immutableFilter, BeeRowSet table) {
    this(display, notificationListener, viewName, columns, null, null, immutableFilter, table);
  }

  public CachedProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, String idColumnName, String versionColumnName,
      Filter immutableFilter, BeeRowSet table) {
    super(display, notificationListener, viewName, columns, idColumnName, versionColumnName,
        immutableFilter);
    Assert.notNull(table);
    this.table = table;
  }

  public void addRow(BeeRow row) {
    if (row != null) {
      table.addRow(row);

      if (getUserFilter() != null && getUserFilter().isMatch(getTable().getColumns(), row)) {
        filteredRowIds.add(row.getId());
        viewRows.add(row);
      }
    }
  }

  @Override
  public void clear() {
    if (getTable().getNumberOfRows() > 0) {
      getTable().clearRows();
    }

    filteredRowIds.clear();
    viewRows.clear();

    super.clear();
  }

  public int getRowCount() {
    if (getUserFilter() == null) {
      return table.getNumberOfRows();
    } else {
      return viewRows.size();
    }
  }

  public BeeRowSet getTable() {
    return table;
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      long id = event.getRowId();
      for (BeeRow row : getTable().getRows()) {
        if (row.getId() == id) {
          event.applyTo(row);
          break;
        }
      }
      super.onCellUpdate(event);
    }
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      for (RowInfo rowInfo : event.getRows()) {
        deleteRow(rowInfo.getId());
      }
      getDisplay().onMultiDelete(event);
      getDisplay().setRowCount(getRowCount(), true);

      onRequest(false);
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      deleteRow(event.getRowId());
      getDisplay().onRowDelete(event);
      getDisplay().setRowCount(getRowCount(), true);

      onRequest(false);
    }
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      addRow(event.getRow());
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (BeeUtils.same(event.getViewName(), getViewName())) {
      BeeRow newRow = event.getRow();
      long id = newRow.getId();

      for (BeeRow oldRow : getTable().getRows()) {
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
    Order order = event.getOrder();
    setOrder(order);
    if (getTable().getNumberOfRows() <= 1) {
      return;
    }

    if (order == null || order.isEmpty()) {
      getTable().sortByRowId(true);
    } else {
      List<Pair<Integer, Boolean>> sortList = Lists.newArrayList();
      int index;

      for (Order.Column sortInfo : order.getColumns()) {
        for (String source : sortInfo.getSources()) {
          if (BeeUtils.same(source, getIdColumnName())) {
            index = DataUtils.ID_INDEX;
          } else if (BeeUtils.same(source, getVersionColumnName())) {
            index = DataUtils.VERSION_INDEX;
          } else {
            index = getTable().getColumnIndex(source);
            if (index < 0) {
              logger.warning("onSort: source", source, "not found");
              continue;
            }
          }
          sortList.add(Pair.of(index, sortInfo.isAscending()));
        }
      }
      if (!sortList.isEmpty()) {
        getTable().sort(sortList);
      }
    }

    if (viewRows.size() > 1) {
      updateViewRows();
    }
    goTop();
  }

  @Override
  public void refresh(final boolean updateActiveRow) {
    String name = getViewName();
    if (BeeUtils.isEmpty(name)) {
      logger.warning("refresh: view name not available");
      return;
    }

    final int oldPageSize = getPageSize();
    final int oldTableSize = getTable().getNumberOfRows();

    Queries.getRowSet(name, null, getQueryFilter(null), getOrder(), new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet rowSet) {
        Assert.notNull(rowSet);
        setTable(rowSet);

        applyFilter(getUserFilter());

        int newTableSize = rowSet.getNumberOfRows();

        int oldRc = getDisplay().getRowCount();
        int newRc = getRowCount();

        if (newTableSize != oldTableSize && oldPageSize >= oldTableSize) {
          getDisplay().setPageSize(newTableSize, oldRc == newRc);
        }
        getDisplay().setRowCount(newRc, true);

        updateDisplay(updateActiveRow);
      }
    });
  }

  @Override
  public void tryFilter(Filter newFilter, Consumer<Boolean> callback, boolean notify) {
    boolean ok = false;

    if (newFilter == null) {
      ok = true;
    } else {
      List<BeeColumn> columns = getTable().getColumns();
      for (BeeRow row : getTable().getRows()) {
        if (newFilter.isMatch(columns, row)) {
          ok = true;
          break;
        }
      }
    }

    if (ok) {
      applyFilter(newFilter);
      acceptFilter(newFilter);

      getDisplay().setPageStart(0, true, false, NavigationOrigin.SYSTEM);
      getDisplay().setRowCount(getRowCount(), true);

      updateDisplay(true);

      if (callback != null) {
        callback.accept(true);
      }

    } else {
      rejectFilter(newFilter, notify);

      if (callback != null) {
        callback.accept(false);
      }
    }
  }

  @Override
  protected void onRequest(boolean updateActiveRow) {
    updateDisplay(updateActiveRow);
  }

  protected void updateDisplay(boolean updateActiveRow) {
    int start = getPageStart();
    int length = getPageSize();
    int rowCount = getRowCount();

    List<BeeRow> rowValues;
    if (rowCount <= 0) {
      rowValues = Lists.newArrayList();
    } else if (length <= 0 || length >= rowCount) {
      rowValues = getRowList();
    } else {
      rowValues = getRowList().subList(start, Math.min(start + length, rowCount));
    }

    if (updateActiveRow) {
      getDisplay().updateActiveRow(rowValues);
    }
    getDisplay().setRowData(rowValues, true);
  }

  private void applyFilter(Filter newFilter) {
    filteredRowIds.clear();
    viewRows.clear();

    if (newFilter != null) {
      List<BeeColumn> columns = getTable().getColumns();
      for (BeeRow row : getTable().getRows()) {
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

      int index = BeeConst.UNDEF;
      for (int i = 0; i < viewRows.size(); i++) {
        if (viewRows.get(i).getId() == rowId) {
          index = i;
          break;
        }
      }

      if (index >= 0) {
        viewRows.remove(index);
      }
    }
  }

  private List<BeeRow> getRowList() {
    if (getUserFilter() == null) {
      return getTable().getRows().getList();
    } else {
      return viewRows;
    }
  }

  private void setTable(BeeRowSet table) {
    this.table = table;
  }

  private void updateViewRows() {
    viewRows.clear();
    if (filteredRowIds.isEmpty()) {
      return;
    }

    for (BeeRow row : getTable().getRows()) {
      if (filteredRowIds.contains(row.getId())) {
        viewRows.add(row);
      }
    }
  }
}
