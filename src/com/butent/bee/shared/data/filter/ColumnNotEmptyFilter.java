package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements a filter which checks for not empty values in a specified column.
 */

public class ColumnNotEmptyFilter extends Filter {

  private String column;

  protected ColumnNotEmptyFilter() {
    super();
  }

  protected ColumnNotEmptyFilter(String column) {
    Assert.notEmpty(column);
    this.column = column;
  }

  @Override
  public void deserialize(String s) {
    setSafe();
    column = s;
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
    ColumnNotEmptyFilter other = (ColumnNotEmptyFilter) obj;

    if (!column.equals(other.column)) {
      return false;
    }
    return true;
  }

  public String getColumn() {
    return column;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + column.hashCode();
    return result;
  }

  @Override
  public boolean involvesColumn(String colName) {
    return BeeUtils.same(colName, column);
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    int columnIndex = getColumnIndex(column, columns);
    Value columnValue = row.getValue(columnIndex, columns.get(columnIndex).getType());
    return !columnValue.isEmpty();
  }

  @Override
  public String serialize() {
    return super.serialize(column);
  }

  @Override
  public String toString() {
    return BeeUtils.joinWords(column, "NOT EMPTY");
  }
}
