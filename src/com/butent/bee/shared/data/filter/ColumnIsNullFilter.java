package com.butent.bee.shared.data.filter;

import com.google.common.collect.Sets;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.column.AbstractColumn;
import com.butent.bee.shared.data.column.AggregationColumn;
import com.butent.bee.shared.data.column.DataTableColumnLookup;
import com.butent.bee.shared.data.column.ScalarFunctionColumn;

import java.util.List;
import java.util.Set;

public class ColumnIsNullFilter extends RowFilter {
  private AbstractColumn column;

  public ColumnIsNullFilter(AbstractColumn column) {
    this.column = column;
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
    ColumnIsNullFilter other = (ColumnIsNullFilter) obj;
    if (column == null) {
      if (other.column != null) {
        return false;
      }
    } else if (!column.equals(other.column)) {
      return false;
    }
    return true;
  }

  @Override
  public List<AggregationColumn> getAggregationColumns() {
    return column.getAllAggregationColumns();
  }

  @Override
  public Set<String> getAllColumnIds() {
    return Sets.newHashSet(column.getAllSimpleColumnIds());
  }
  
  public AbstractColumn getColumn() {
    return column;
  }

  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    return column.getAllScalarFunctionColumns();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((column == null) ? 0 : column.hashCode());
    return result;
  }

  @Override
  public boolean isMatch(IsTable<?, ?> table, IsRow row) {
    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    return column.getValue(lookup, row).isNull();
  }

  @Override
  public String toQueryString() {
    return column.toQueryString() + " IS NULL";
  }
}
