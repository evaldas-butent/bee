package com.butent.bee.shared.data.column;

import com.google.common.collect.Lists;

import com.butent.bee.shared.data.DataTable;
import com.butent.bee.shared.data.value.ValueType;

import java.util.List;

public class SimpleColumn extends AbstractColumn {
  private String columnId;

  public SimpleColumn(String columnId) {
    this.columnId = columnId;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SimpleColumn) {
      SimpleColumn other = (SimpleColumn) o;
      return columnId.equals(other.columnId);
    }
    return false;
  }

  @Override
  public List<AggregationColumn> getAllAggregationColumns() {
    return Lists.newArrayList();
  }

  @Override
  public List<ScalarFunctionColumn> getAllScalarFunctionColumns() {
    return Lists.newArrayList();
  }

  @Override
  public List<String> getAllSimpleColumnIds() {
    return Lists.newArrayList(columnId);
  }

  @Override
  public List<SimpleColumn> getAllSimpleColumns() {
    return Lists.newArrayList(this);
  }

  public String getColumnId() {
    return columnId;
  }

  @Override
  public String getId() {
    return columnId;
  }

  @Override
  public ValueType getValueType(DataTable dataTable) {
    return dataTable.getColumn(columnId).getType();
  }

  @Override
  public int hashCode() {
    int hash  = 1279;
    hash = (hash * 17) + columnId.hashCode();
    return hash;
  }

  @Override
  public String toQueryString() {
    if (columnId.contains("`")) {
      throw new RuntimeException("Column ID cannot contain backtick (`)");
    }
    return "`" + columnId + "`";
  }

  @Override
  public String toString() {
    return columnId;
  }

  @Override
  public void validateColumn(DataTable dataTable) {
  }
}
