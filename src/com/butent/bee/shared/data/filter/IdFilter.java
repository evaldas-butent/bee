package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.LongValue;

import java.util.List;

/**
 * Enables to compare ID column against some value.
 */
public class IdFilter extends ColumnValueFilter {

  protected IdFilter() {
    super();
  }

  protected IdFilter(Operator operator, long value) {
    super("ID", operator, new LongValue(value));
  }

  @Override
  public boolean involvesColumn(String colName) {
    return false;
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    return isOperatorMatch(new LongValue(row.getId()), getValue());
  }
}
