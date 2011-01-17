package com.butent.bee.shared.data.filter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.column.AggregationColumn;
import com.butent.bee.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

public class CompoundFilter extends RowFilter {
  public static enum LogicalOperator { AND, OR }

  private List<RowFilter> subFilters;
  private LogicalOperator operator;

  public CompoundFilter(LogicalOperator operator, List<RowFilter> subFilters) {
    this.subFilters = subFilters;
    this.operator = operator;
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
    CompoundFilter other = (CompoundFilter) obj;
    if (operator == null) {
      if (other.operator != null) {
        return false;
      }
    } else if (!operator.equals(other.operator)) {
      return false;
    }
    if (subFilters == null) {
      if (other.subFilters != null) {
        return false;
      }
    } else if (!subFilters.equals(other.subFilters)) {
      return false;
    }
    return true;
  }

  @Override
  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (RowFilter subFilter : subFilters) {
      result.addAll(subFilter.getAggregationColumns());
    }
    return result;
  }

  @Override
  public Set<String> getAllColumnIds() {
    Set<String> result = Sets.newHashSet();
    for (RowFilter subFilter : subFilters) {
      result.addAll(subFilter.getAllColumnIds());
    }
    return result;
  }
  
  public LogicalOperator getOperator() {
    return operator;
  }

  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (RowFilter subFilter : subFilters) {
      result.addAll(subFilter.getScalarFunctionColumns());
    }
    return result;
  }

  public List<RowFilter> getSubFilters() {
    return ImmutableList.copyOf(subFilters);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((operator == null) ? 0 : operator.hashCode());
    result = prime * result + ((subFilters == null) ? 0 : subFilters.hashCode());
    return result;
  }

  @Override
  public boolean isMatch(IsTable table, IsRow row) {
    if (subFilters.isEmpty()) {
      throw new RuntimeException("Compound filter with empty subFilters list");
    }
    for (RowFilter subFilter : subFilters) {
      boolean result = subFilter.isMatch(table, row);
      if (((operator == LogicalOperator.AND) && !result) ||
          ((operator == LogicalOperator.OR) && result)) {
        return result;
      }
    }
    return (operator == LogicalOperator.AND);
  }

  @Override
  public String toQueryString() {
    List<String> subFilterStrings = Lists.newArrayList();
    for (RowFilter filter : subFilters) {
      subFilterStrings.add("(" + filter.toQueryString() + ")");
    }
    return BeeUtils.append(new StringBuilder(), subFilterStrings,
        " " + operator.name() + " ").toString();
  }
}
