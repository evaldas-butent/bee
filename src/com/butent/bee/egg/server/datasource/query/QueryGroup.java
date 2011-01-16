package com.butent.bee.egg.server.datasource.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.butent.bee.egg.shared.data.column.AbstractColumn;
import com.butent.bee.egg.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.egg.shared.data.column.SimpleColumn;

import java.util.List;

public class QueryGroup {
  private List<AbstractColumn> columns;

  public QueryGroup() {
    columns = Lists.newArrayList();
  }

  public void addColumn(AbstractColumn column) {
    columns.add(column);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    QueryGroup other = (QueryGroup) obj;
    if (columns == null) {
      if (other.columns != null) {
        return false;
      }
    } else if (!columns.equals(other.columns)) {
      return false;
    }
    return true;
  }

  public List<String> getColumnIds() {
    List<String> columnIds = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      columnIds.add(col.getId());
    }
    return ImmutableList.copyOf(columnIds);
  }

  public List<AbstractColumn> getColumns() {
    return ImmutableList.copyOf(columns);
  }

  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> scalarFunctionColumns = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      scalarFunctionColumns.addAll(col.getAllScalarFunctionColumns());
    }
    return scalarFunctionColumns;
  }

  public List<String> getSimpleColumnIds() {
    List<String> columnIds = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      columnIds.addAll(col.getAllSimpleColumnIds());
    }
    return columnIds;
  }

  public List<SimpleColumn> getSimpleColumns() {
    List<SimpleColumn> simpleColumns = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      simpleColumns.addAll(col.getAllSimpleColumns());
    }
    return simpleColumns;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columns == null) ? 0 : columns.hashCode());
    return result;
  }
  
  public String toQueryString() {
    return Query.columnListToQueryString(columns); 
  }
}
