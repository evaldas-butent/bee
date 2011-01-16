package com.butent.bee.egg.server.datasource.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.butent.bee.egg.shared.data.column.AbstractColumn;
import com.butent.bee.egg.shared.data.column.AggregationColumn;
import com.butent.bee.egg.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.egg.shared.data.column.SimpleColumn;

import java.util.List;

public class QuerySelection {
  private List<AbstractColumn> columns;

  public QuerySelection() {
    columns = Lists.newArrayList();
  }

  public QuerySelection(QuerySelection source) {
    columns = Lists.newArrayList(source.columns);
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
    QuerySelection other = (QuerySelection) obj;
    if (columns == null) {
      if (other.columns != null) {
        return false;
      }
    } else if (!columns.equals(other.columns)) {
      return false;
    }
    return true;
  }

  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      result.addAll(col.getAllAggregationColumns());
    }
    return result;
  }

  public List<AbstractColumn> getColumns() {
    return ImmutableList.copyOf(columns);
  }

  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      result.addAll(col.getAllScalarFunctionColumns());
    }
    return result;
  }

  public List<SimpleColumn> getSimpleColumns() {
    List<SimpleColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      result.addAll(col.getAllSimpleColumns());
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columns == null) ? 0 : columns.hashCode());
    return result;
  }

  public boolean isEmpty() {
    return columns.isEmpty();
  }
  
  public String toQueryString() {
    return Query.columnListToQueryString(columns); 
  }
}
