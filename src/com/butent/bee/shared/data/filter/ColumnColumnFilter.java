package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Enables to compare values in two columns.
 */
public class ColumnColumnFilter extends ComparisonFilter {

  protected ColumnColumnFilter() {
    super();
  }

  protected ColumnColumnFilter(String leftColumn, Operator operator, String rightColumn) {
    super(leftColumn, operator.isStringOperator() ? Operator.EQ : operator, rightColumn);
  }

  @Override
  public String getValue() {
    return (String) super.getValue();
  }

  @Override
  public boolean involvesColumn(String colName) {
    return BeeUtils.inListSame(colName, getColumn(), getValue());
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    int firstIndex = getColumnIndex(getColumn(), columns);
    Value firstValue = row.getValue(firstIndex, columns.get(firstIndex).getType());
    int secondIndex = getColumnIndex(getValue(), columns);
    Value secondValue = row.getValue(secondIndex, columns.get(secondIndex).getType());
    return isOperatorMatch(firstValue, secondValue);
  }

  @Override
  protected Object restoreValue(String s) {
    return s;
  }
}