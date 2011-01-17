package com.butent.bee.client.grid;

import com.butent.bee.shared.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TableDefinition<RowType> {
  private List<ColumnDefinition<RowType, ?>> columnDefs;
  private Set<ColumnDefinition<RowType, ?>> hiddenColumnDefs;

  public TableDefinition() {
    this(new ArrayList<ColumnDefinition<RowType, ?>>());
  }

  public TableDefinition(List<ColumnDefinition<RowType, ?>> columnDefs) {
    this.columnDefs = columnDefs;
    hiddenColumnDefs = new HashSet<ColumnDefinition<RowType, ?>>();
  }

  public void addColumnDefinition(ColumnDefinition<RowType, ?> columnDef) {
    columnDefs.add(columnDef);
  }

  public void addColumnDefinition(int index, ColumnDefinition<RowType, ?> columnDef) {
    columnDefs.add(index, columnDef);
  }

  public ColumnDefinition<RowType, ?> getColumnDefinition(int column) {
    Assert.isIndex(columnDefs, column);
    return columnDefs.get(column);
  }

  public int getColumnDefinitionCount() {
    return columnDefs.size();
  }

  public List<ColumnDefinition<RowType, ?>> getColumnDefs() {
    return columnDefs;
  }

  public int getColumnId(int column) {
    return getColumnDefinition(column).getColumnId();
  }

  public List<ColumnDefinition<RowType, ?>> getVisibleColumnDefinitions() {
    List<ColumnDefinition<RowType, ?>> visibleColumns = 
      new ArrayList<ColumnDefinition<RowType, ?>>();
    for (ColumnDefinition<RowType, ?> columnDef : columnDefs) {
      if (isColumnVisible(columnDef)) {
        visibleColumns.add(columnDef);
      }
    }
    return visibleColumns;
  }

  public boolean isColumnVisible(int column) {
    return isColumnVisible(getColumnDefinition(column));
  }

  public boolean isColumnVisible(ColumnDefinition<RowType, ?> colDef) {
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

  public void removeColumnDefinition(ColumnDefinition<RowType, ?> columnDef) {
    columnDefs.remove(columnDef);
  }

  public void renderRows(int startRowIndex, Iterator<RowType> rowValues, RowView<RowType> view) {
    List<ColumnDefinition<RowType, ?>> visibleColumns = getVisibleColumnDefinitions();
    view.renderRowsImpl(startRowIndex, rowValues, visibleColumns);
  }

  public void setColumnDefs(List<ColumnDefinition<RowType, ?>> columnDefs) {
    this.columnDefs = columnDefs;
  }

  public void setColumnVisible(int column, boolean visible) {
    setColumnVisible(getColumnDefinition(column), visible);
  }

  public void setColumnVisible(ColumnDefinition<RowType, ?> colDef, boolean visible) {
    if (visible) {
      hiddenColumnDefs.remove(colDef);
    } else {
      hiddenColumnDefs.add(colDef);
    }
  }
}
