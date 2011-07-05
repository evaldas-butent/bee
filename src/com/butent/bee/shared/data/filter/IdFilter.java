package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Enables to compare ID column against some value.
 */
public class IdFilter extends ComparisonFilter {

  protected IdFilter() {
    super();
  }

  protected IdFilter(String column, Operator operator, long value) {
    super(column, operator.isStringOperator() ? Operator.EQ : operator, value);
  }

  public Long getValue() {
    return (Long) super.getValue();
  }

  @Override
  public boolean involvesColumn(String colName) {
    return false;
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    return isOperatorMatch(new LongValue(row.getId()), new LongValue(getValue()));
  }

  @Override
  protected Object restoreValue(String s) {
    return BeeUtils.toLong(s);
  }
}
