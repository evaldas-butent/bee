package com.butent.bee.egg.server.datasource.query;

import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableRow;

import java.util.List;
import java.util.Set;

public class NegationFilter extends QueryFilter {
  private QueryFilter subFilter;

  public NegationFilter(QueryFilter subFilter) {
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
  public Set<String> getAllColumnIds() {
    return subFilter.getAllColumnIds();
  }

  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    return subFilter.getScalarFunctionColumns();
  }

  public QueryFilter getSubFilter() {
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
  public boolean isMatch(DataTable table, TableRow row) {
    return !subFilter.isMatch(table, row);
  }

  @Override
  public String toQueryString() {
    return "NOT (" + subFilter.toQueryString() + ")";
  }

  @Override
  protected List<AggregationColumn> getAggregationColumns() {
    return subFilter.getAggregationColumns();
  }
}
