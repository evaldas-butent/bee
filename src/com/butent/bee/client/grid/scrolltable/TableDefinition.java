package com.butent.bee.client.grid.scrolltable;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Generates a set of column definitions for a table and operates with it.
 */

public class TableDefinition {
  private List<ColumnDefinition> columnDefs;
  private Set<ColumnDefinition> hiddenColumnDefs;

  public TableDefinition() {
    this(new ArrayList<ColumnDefinition>());
  }

  public TableDefinition(List<ColumnDefinition> columnDefs) {
    this.columnDefs = columnDefs;
    hiddenColumnDefs = new HashSet<ColumnDefinition>();
  }

  public void addColumnDefinition(ColumnDefinition columnDef) {
    columnDefs.add(columnDef);
  }

  public void addColumnDefinition(int index, ColumnDefinition columnDef) {
    columnDefs.add(index, columnDef);
  }

  public ColumnDefinition getColumnDefinition(int column) {
    Assert.isIndex(columnDefs, column);
    return columnDefs.get(column);
  }

  public int getColumnDefinitionCount() {
    return columnDefs.size();
  }

  public List<ColumnDefinition> getColumnDefs() {
    return columnDefs;
  }

  public int getColumnId(int column) {
    return getColumnDefinition(column).getColumnId();
  }

  public List<ColumnDefinition> getVisibleColumnDefinitions() {
    List<ColumnDefinition> visibleColumns = new ArrayList<ColumnDefinition>();
    for (ColumnDefinition columnDef : columnDefs) {
      if (isColumnVisible(columnDef)) {
        visibleColumns.add(columnDef);
      }
    }
    return visibleColumns;
  }

  public boolean isColumnVisible(int column) {
    return isColumnVisible(getColumnDefinition(column));
  }

  public boolean isColumnVisible(ColumnDefinition colDef) {
    return !hiddenColumnDefs.contains(colDef);
  }

  public void moveColumnDef(int srcId, int dstId) {
    Assert.isTrue(srcId != dstId);
    int fr = -1;
    int to = -1;
    int id;
    int n = columnDefs.size();

    for (int i = 0; i < n; i++) {
      id = columnDefs.get(i).getColumnId();

      if (id == srcId) {
        fr = i;
      } else if (id == dstId) {
        to = i;
      }
    }
    Assert.nonNegative(fr);
    Assert.nonNegative(to);
    Assert.isTrue(fr != to);

    for (int i = 0; i < n; i++) {
      columnDefs.get(i).setColumnOrder(i);
    }

    if (fr < to) {
      for (int i = fr + 1; i <= to; i++) {
        columnDefs.get(i).setColumnOrder(i - 1);
      }
    } else {
      for (int i = to; i < fr; i++) {
        columnDefs.get(i).setColumnOrder(i + 1);
      }
    }
    columnDefs.get(fr).setColumnOrder(to);

    orderColumnDefs();
  }

  public void orderColumnDefs() {
    Collections.sort(columnDefs);
  }

  public void removeColumnDefinition(ColumnDefinition columnDef) {
    columnDefs.remove(columnDef);
  }

  public void renderRows(int startRowIndex, Iterator<IsRow> rowValues, RowView view) {
    List<ColumnDefinition> visibleColumns = getVisibleColumnDefinitions();
    view.renderRowsImpl(startRowIndex, rowValues, visibleColumns);
  }

  public void setColumnDefs(List<ColumnDefinition> columnDefs) {
    this.columnDefs = columnDefs;
  }

  public void setColumnVisible(int column, boolean visible) {
    setColumnVisible(getColumnDefinition(column), visible);
  }

  public void setColumnVisible(ColumnDefinition colDef, boolean visible) {
    if (visible) {
      hiddenColumnDefs.remove(colDef);
    } else {
      hiddenColumnDefs.add(colDef);
    }
  }
}
