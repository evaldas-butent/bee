package com.butent.bee.egg.server.datasource.query.engine;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;

import com.butent.bee.egg.server.datasource.query.AbstractColumn;

import java.util.List;

class ColumnIndices {
  private ArrayListMultimap<AbstractColumn, Integer> columnToIndices;

  public ColumnIndices() {
    columnToIndices = ArrayListMultimap.create();
  }

  public void clear() {
    columnToIndices.clear();
  }

  public int getColumnIndex(AbstractColumn col) {
    List<Integer> indices = columnToIndices.get(col);
    if (indices.size() != 1) {
      throw new RuntimeException("Invalid use of ColumnIndices.");
    }
    return indices.get(0);
  }

  public List<Integer> getColumnIndices(AbstractColumn col) {
    return ImmutableList.copyOf(columnToIndices.get(col));
  }

  public void put(AbstractColumn col, int index) {
    columnToIndices.put(col, index);
  }
}
