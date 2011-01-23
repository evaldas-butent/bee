package com.butent.bee.shared.data.sort;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.butent.bee.shared.data.column.AbstractColumn;
import com.butent.bee.shared.data.column.AggregationColumn;
import com.butent.bee.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class SortQuery {
  private List<SortColumn> sortColumns;

  public SortQuery() {
    sortColumns = Lists.newArrayList();
  }

  public void addSort(AbstractColumn column, SortOrder order) {
    addSort(new SortColumn(column, order));
  }

  public void addSort(SortColumn columnSort) {
    sortColumns.add(columnSort);
  }
  
  public boolean containsColumn(AbstractColumn column) {
    boolean found = false;
    for (SortColumn info : sortColumns) {
      if (info.getColumn().equals(column)) {
        found = true;
        break;
      }
    }
    return found;
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
    SortQuery other = (SortQuery) obj;
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
    for (SortColumn columnSort : sortColumns) {
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
    for (SortColumn columnSort : sortColumns) {
      result.add(columnSort.getColumn());
    }
    return result;
  }

  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (SortColumn columnSort : sortColumns) {
      AbstractColumn col = columnSort.getColumn();
      for (ScalarFunctionColumn innerCol : col.getAllScalarFunctionColumns()) {
        if (!result.contains(innerCol)) {
          result.add(innerCol);
        }
      }
    }
    return result;
  }

  public List<SortColumn> getSortColumns() {
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
    for (SortColumn colSort : sortColumns) {
      stringList.add(colSort.toQueryString());
    }
    BeeUtils.append(builder, stringList, ", ");
    return builder.toString();
  }
}
