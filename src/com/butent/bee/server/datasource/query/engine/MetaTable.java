package com.butent.bee.server.datasource.query.engine;

import com.google.common.collect.Maps;

import com.butent.bee.shared.data.TableCell;

import java.util.Map;

class MetaTable {
  private Map<RowTitle, Map<ColumnTitle, TableCell>> data;

  public MetaTable() {
    data = Maps.newHashMap();
  }

  public TableCell getCell(RowTitle rowTitle, ColumnTitle columnTitle) {
    Map<ColumnTitle, TableCell> rowData = data.get(rowTitle);
    if (rowData == null) {
      return null;
    }
    return rowData.get(columnTitle);
  }

  public Map<ColumnTitle, TableCell> getRow(RowTitle rowTitle) {
    return data.get(rowTitle);
  }

  public boolean isEmpty() {
    return data.isEmpty();
  }

  public void put(RowTitle rowTitle, ColumnTitle columnTitle, TableCell cell) {
    Map<ColumnTitle, TableCell> rowData = data.get(rowTitle);
    if (rowData == null) {
      rowData = Maps.newHashMap();
      data.put(rowTitle, rowData);
    }
    rowData.put(columnTitle, cell);
  }
}
