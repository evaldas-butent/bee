package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.column.AggregationColumn;
import com.butent.bee.shared.data.column.ScalarFunctionColumn;

import java.util.List;
import java.util.Set;

public class NegationFilter extends RowFilter {
  private RowFilter subFilter;

  public NegationFilter(RowFilter subFilter) {
    this.subFilter = subFilter;
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
    NegationFilter other = (NegationFilter) obj;
    if (subFilter == null) {
      if (other.subFilter != null) {
        return false;
      }
    } else if (!subFilter.equals(other.subFilter)) {
      return false;
    }
    return true;
  }

  @Override
  public List<AggregationColumn> getAggregationColumns() {
    return subFilter.getAggregationColumns();
  }

  @Override
  public Set<String> getAllColumnIds() {
    return subFilter.getAllColumnIds();
  }

  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    return subFilter.getScalarFunctionColumns();
  }

  public RowFilter getSubFilter() {
    return subFilter;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((subFilter == null) ? 0 : subFilter.hashCode());
    return result;
  }

  @Override
  public boolean isMatch(IsTable<?, ?> table, IsRow row) {
    return !subFilter.isMatch(table, row);
  }

  @Override
  public String toQueryString() {
    return "NOT (" + subFilter.toQueryString() + ")";
  }
}
