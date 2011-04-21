package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;

public class ColumnIsEmptyFilter extends Filter {

  private String column;

  public ColumnIsEmptyFilter(String column) {
    Assert.notEmpty(column);
    this.column = column;
  }

  protected ColumnIsEmptyFilter() {
    super();
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
    ColumnIsEmptyFilter other = (ColumnIsEmptyFilter) obj;

    if (!column.equals(other.column)) {
      return false;
    }
    return true;
  }

  public String getColumn() {
    return column;
  }

  @Override
  public IsCondition getCondition(Map<String, String[]> columns) {
    IsCondition condition = null;

    String colName = column.toLowerCase();
    Assert.contains(columns, colName);

    String als = columns.get(colName)[0];

    if (!BeeUtils.isEmpty(als)) {
      String fld = columns.get(colName)[1];
      condition = SqlUtils.isNull(als, fld); // TODO: compare according type
    }
    return condition;
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
    Value columnValue = row.getValue(getColumnIndex(column, columns));
    return columnValue.isNull() || BeeUtils.isEmpty(columnValue.getObjectValue());
  }

  @Override
  public String serialize() {
    return Codec.beeSerializeAll(BeeUtils.getClassName(this.getClass()), column);
  }

  @Override
  public String toString() {
    return BeeUtils.concat(1, column, "IS EMPTY");
  }
}
