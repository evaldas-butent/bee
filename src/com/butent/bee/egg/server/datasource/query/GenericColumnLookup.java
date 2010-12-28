package com.butent.bee.egg.server.datasource.query;

import com.google.common.collect.Maps;

import java.util.Map;

public class GenericColumnLookup implements ColumnLookup {
  private Map<AbstractColumn, Integer> columnIndexByColumn;

  public GenericColumnLookup() {
    columnIndexByColumn = Maps.newHashMap();
  }

  public void clear() {
    columnIndexByColumn.clear();
  }

  @Override
  public boolean containsColumn(AbstractColumn column) {
    return columnIndexByColumn.containsKey(column);
  }

  public int getColumnIndex(AbstractColumn column) {
    return columnIndexByColumn.get(column);
  }

  public void put(AbstractColumn col, int index) {
    columnIndexByColumn.put(col, index);
  }
}
