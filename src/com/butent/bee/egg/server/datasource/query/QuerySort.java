package com.butent.bee.egg.server.datasource.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.butent.bee.egg.shared.data.SortInfo;
import com.butent.bee.egg.shared.data.SortOrder;
import com.butent.bee.egg.shared.data.column.AbstractColumn;
import com.butent.bee.egg.shared.data.column.AggregationColumn;
import com.butent.bee.egg.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public class QuerySort {
  private List<SortInfo> sortColumns;

  public QuerySort() {
    sortColumns = Lists.newArrayList();
  }

  public void addSort(AbstractColumn column, SortOrder order) {
    addSort(new SortInfo(column, order));
  }

  public void addSort(SortInfo columnSort) {
    sortColumns.add(columnSort);
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
    QuerySort other = (QuerySort) obj;
    if (sortColumns == null) {
      if (other.sortColumns != null) {
        return false;
      }
    } else if (!sortColumns.equals(other.sortColumns)) {
      return false;
    }
    return true;
  }

  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (SortInfo columnSort : sortColumns) {
      AbstractColumn col = columnSort.getColumn();
      for (AggregationColumn innerCol : col.getAllAggregationColumns()) {
        if (!result.contains(innerCol)) {
          result.add(innerCol);
        }
      }
    }
    return result;
  }

  public List<AbstractColumn> getColumns() {
    List<AbstractColumn> result = Lists.newArrayListWithExpectedSize(sortColumns.size());
    for (SortInfo columnSort : sortColumns) {
      result.add(columnSort.getColumn());
    }
    return result;
  }

  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (SortInfo columnSort : sortColumns) {
      AbstractColumn col = columnSort.getColumn();
      for (ScalarFunctionColumn innerCol : col.getAllScalarFunctionColumns()) {
        if (!result.contains(innerCol)) {
          result.add(innerCol);
        }
      }
    }
    return result;
  }

  public List<SortInfo> getSortColumns() {
    return ImmutableList.copyOf(sortColumns);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sortColumns == null) ? 0 : sortColumns.hashCode());
    return result;
  }

  public boolean isEmpty() {
    return sortColumns.isEmpty();
  }
  
  public String toQueryString() {
    StringBuilder builder = new StringBuilder();
    List<String> stringList = Lists.newArrayList();
    for (SortInfo colSort : sortColumns) {
      stringList.add(colSort.toQueryString());
    }
    BeeUtils.append(builder, stringList, ", ");
    return builder.toString();
  }
}
