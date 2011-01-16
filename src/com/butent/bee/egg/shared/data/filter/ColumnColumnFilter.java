package com.butent.bee.egg.shared.data.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.egg.shared.data.IsRow;
import com.butent.bee.egg.shared.data.IsTable;
import com.butent.bee.egg.shared.data.column.AbstractColumn;
import com.butent.bee.egg.shared.data.column.AggregationColumn;
import com.butent.bee.egg.shared.data.column.DataTableColumnLookup;
import com.butent.bee.egg.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.egg.shared.data.value.Value;

import java.util.List;
import java.util.Set;

public class ColumnColumnFilter extends ComparisonFilter {

  private AbstractColumn firstColumn;
  private AbstractColumn secondColumn;

  public ColumnColumnFilter(AbstractColumn firstColumn,
      AbstractColumn secondColumn, Operator operator) {
    super(operator);
    this.firstColumn = firstColumn;
    this.secondColumn = secondColumn;
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
    ColumnColumnFilter other = (ColumnColumnFilter) obj;
    if (firstColumn == null) {
      if (other.firstColumn != null) {
        return false;
      }
    } else if (!firstColumn.equals(other.firstColumn)) {
      return false;
    }
    if (secondColumn == null) {
      if (other.secondColumn != null) {
        return false;
      }
    } else if (!secondColumn.equals(other.secondColumn)) {
      return false;
    }
    return true;
  }

  @Override
  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> aggregationColumns = Lists.newArrayList();
    aggregationColumns.addAll(firstColumn.getAllAggregationColumns());
    aggregationColumns.addAll(secondColumn.getAllAggregationColumns());
    return aggregationColumns;
  }

  @Override
  public Set<String> getAllColumnIds() {
    Set<String> columnIds = Sets.newHashSet(firstColumn.getAllSimpleColumnIds());
    columnIds.addAll(secondColumn.getAllSimpleColumnIds());
    return columnIds;
  }

  public AbstractColumn getFirstColumn() {
    return firstColumn;
  }

  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> scalarFunctionColumns = Lists.newArrayList();
    scalarFunctionColumns.addAll(firstColumn.getAllScalarFunctionColumns());
    scalarFunctionColumns.addAll(secondColumn.getAllScalarFunctionColumns());
    return scalarFunctionColumns;
  }

  public AbstractColumn getSecondColumn() {
    return secondColumn;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((firstColumn == null) ? 0 : firstColumn.hashCode());
    result = prime * result + ((secondColumn == null) ? 0 : secondColumn.hashCode());
    return result;
  }

  @Override
  public boolean isMatch(IsTable table, IsRow row) {
    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    Value firstValue = firstColumn.getValue(lookup, row);
    Value secondValue = secondColumn.getValue(lookup, row);
    return isOperatorMatch(firstValue, secondValue);
  }

  @Override
  public String toQueryString() {
    return firstColumn.toQueryString() + " " + operator.toQueryString() + " "
        + secondColumn.toQueryString();
  }
}
