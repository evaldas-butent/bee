package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.shared.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TableDefinition<RowType> {
  private List<ColumnDefinition<RowType, ?>> columnDefs;
  private Set<ColumnDefinition<RowType, ?>> hiddenColumnDefs;

  private RowRenderer<RowType> rowRenderer = new DefaultRowRenderer<RowType>();

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

  public RowRenderer<RowType> getRowRenderer() {
    return rowRenderer;
  }

  public List<ColumnDefinition<RowType, ?>> getVisibleColumnDefinitions() {
    List<ColumnDefinition<RowType, ?>> visibleColumns = new ArrayList<ColumnDefinition<RowType, ?>>();
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

  public void removeColumnDefinition(ColumnDefinition<RowType, ?> columnDef) {
    columnDefs.remove(columnDef);
  }

  public void renderRows(int startRowIndex, Iterator<RowType> rowValues,
      AbstractRowView<RowType> view) {
    List<ColumnDefinition<RowType, ?>> visibleColumns = getVisibleColumnDefinitions();
    view.renderRowsImpl(startRowIndex, rowValues, rowRenderer, visibleColumns);
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

  public void setRowRenderer(RowRenderer<RowType> rowRenderer) {
    Assert.notNull(rowRenderer, "rowRenderer cannot be null");
    this.rowRenderer = rowRenderer;
  }

}
