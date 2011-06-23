package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.StringTokenizer;

/**
 * Is an abstract class, extends {@code Filter) class, determines implementation of comparison
 * filters.

 */

public abstract class ComparisonFilter extends Filter {

  public static Filter compareWithColumn(IsColumn left, Operator op, IsColumn right) {
    Assert.noNulls(left, right);
    String leftColumn = left.getId();
    ValueType leftType = left.getType();
    String rightColumn = right.getId();
    ValueType rightType = right.getType();

    if (!BeeUtils.same(leftType.getGroupCode(), rightType.getGroupCode())) {
      LogUtils.warning(LogUtils.getDefaultLogger(),
          "Incompatible column types: " +
              leftColumn + BeeUtils.parenthesize(leftType) + " AND " +
              rightColumn + BeeUtils.parenthesize(rightType));
      return null;
    }
    return new ColumnColumnFilter(leftColumn, op, rightColumn);
  }

  public static Filter compareWithValue(IsColumn column, Operator op, String value) {
    Assert.notNull(column);

    if (ValueType.isNumeric(column.getType()) && !BeeUtils.isDouble(value)) {
      LogUtils.warning(LogUtils.getDefaultLogger(), "Not a numeric value: " + value);
      return null;
    }
    return new ColumnValueFilter(column.getId(), op,
        Value.parseValue(column.getType(), value, true));
  }

  private Operator operator;

  protected ComparisonFilter() {
    super();
  }

  protected ComparisonFilter(Operator operator) {
    Assert.notEmpty(operator);
    this.operator = operator;
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
    ComparisonFilter other = (ComparisonFilter) obj;

    if (!operator.equals(other.operator)) {
      return false;
    }
    return true;
  }

  public Operator getOperator() {
    return operator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + operator.hashCode();
    return result;
  }

  protected boolean isOperatorMatch(Value v1, Value v2) {
    if (v1.isNull() || v2.isNull()) {
      return false;
    }
    switch (operator) {
      case EQ:
        return (v1.compareTo(v2) == 0);
      case NE:
        return (v1.compareTo(v2) != 0);
      case LT:
        return (v1.compareTo(v2) < 0);
      case GT:
        return (v1.compareTo(v2) > 0);
      case LE:
        return (v1.compareTo(v2) <= 0);
      case GE:
        return (v1.compareTo(v2) >= 0);
      case STARTS:
        return v1.toString().toLowerCase().startsWith(v2.toString().toLowerCase());
      case ENDS:
        return v1.toString().toLowerCase().endsWith(v2.toString().toLowerCase());
      case CONTAINS:
        return v1.toString().toLowerCase().contains(v2.toString().toLowerCase());
      case MATCHES:
        return isLike(v1.toString().toLowerCase(), v2.toString().toLowerCase());

      default:
        Assert.unsupported();
    }
    return false;
  }

  protected void setOperator(Operator operator) {
    this.operator = operator;
  }

  private boolean isLike(String s1, String s2) {
    StringTokenizer tokenizer =
        new StringTokenizer(s2, Operator.CHAR_ANY + Operator.CHAR_ONE, true);
    StringBuilder regexp = new StringBuilder();

    while (tokenizer.hasMoreTokens()) {
      String s = tokenizer.nextToken();

      if (s.equals(Operator.CHAR_ANY)) {
        regexp.append(".*");
      } else if (s.equals(Operator.CHAR_ONE)) {
        regexp.append(".");
      } else {
        for (char c : s.toCharArray()) {
          regexp.append("\\").append(c);
        }
      }
    }
    return s1.matches(regexp.toString());
  }
}