package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Enables to compare column data against given value (for example quantity > 10).
 */
public class ColumnValueFilter extends ComparisonFilter {

  protected ColumnValueFilter() {
    super();
  }

  ColumnValueFilter(String column, Operator operator, Value value) {
    super(column,
        operator.isStringOperator() && !ValueType.isString(value.getType())
            ? Operator.EQ
            : operator,
        value);
  }

  public Value getValue() {
    return (Value) super.getValue();
  }

  @Override
  public boolean involvesColumn(String colName) {
    return BeeUtils.same(colName, getColumn());
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    int columnIndex = getColumnIndex(getColumn(), columns);
    Value columnValue = row.getValue(columnIndex, columns.get(columnIndex).getType());
    return isOperatorMatch(columnValue, getValue());
  }

  @Override
  public String toString() {
    String value = getValue().getString();

    if (ValueType.isString(getValue().getType())) {
      value = "\"" + value + "\"";
    }
    return BeeUtils.concat(0, getColumn(), getOperator().toTextString(), value);
  }

  @Override
  protected Object restoreValue(String s) {
    return Value.restore(s);
  }
}