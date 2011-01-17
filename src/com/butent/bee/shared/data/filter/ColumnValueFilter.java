package com.butent.bee.shared.data.filter;

import com.google.common.collect.Sets;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.column.AbstractColumn;
import com.butent.bee.shared.data.column.AggregationColumn;
import com.butent.bee.shared.data.column.DataTableColumnLookup;
import com.butent.bee.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.shared.data.value.Value;

import java.util.List;
import java.util.Set;

public class ColumnValueFilter extends ComparisonFilter {
  private AbstractColumn column;
  private Value value;
  private boolean isComparisonOrderReversed;

  public ColumnValueFilter(AbstractColumn column, Value value, Operator operator) {
    this(column, value, operator, false);
  }

  public ColumnValueFilter(AbstractColumn column, Value value,
      Operator operator, boolean isComparisonOrderReversed) {
    super(operator);
    this.column = column;
    this.value = value;
    this.isComparisonOrderReversed = isComparisonOrderReversed;
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
    ColumnValueFilter other = (ColumnValueFilter) obj;
    if (column == null) {
      if (other.column != null) {
        return false;
      }
    } else if (!column.equals(other.column)) {
      return false;
    }
    if (isComparisonOrderReversed != other.isComparisonOrderReversed) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
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

  public Value getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((column == null) ? 0 : column.hashCode());
    result = prime * result + (isComparisonOrderReversed ? 1231 : 1237);
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  public boolean isComparisonOrderReversed() {
    return isComparisonOrderReversed;
  }

  @Override
  public boolean isMatch(IsTable table, IsRow row) {
    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    Value columnValue = column.getValue(lookup, row);
    return isComparisonOrderReversed ? isOperatorMatch(value, columnValue) :
        isOperatorMatch(columnValue, value);
  }

  @Override
  public String toQueryString() {
    if (isComparisonOrderReversed) {
      return value.toQueryString() + " " + operator.toQueryString() + " "
          + column.toQueryString();
    } else {
      return column.toQueryString() + " " + operator.toQueryString() + " "
          + value.toQueryString();
    }
  }
}
