package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;

import java.util.StringTokenizer;

/**
 * Is an abstract class, extends {@code Filter) class, determines implementation of comparison
 * filters.

 */
public abstract class ComparisonFilter extends Filter {

  /**
   * Contains a list of filter parts which go through serialization.
   */
  private enum Serial {
    COLUMN, OPERATOR, VALUE
  }

  public static Filter compareId(long value) {
    return compareId(Operator.EQ, value);
  }

  public static Filter compareId(Operator op, long value) {
    Assert.notNull(op);

    return new IdFilter(op, value);
  }

  public static Filter compareId(Operator op, String value) {
    if (!BeeUtils.isLong(value)) {
      LogUtils.warning(LogUtils.getDefaultLogger(), "Not an ID value:", value);
      return null;
    }
    return compareId(op, BeeUtils.toLong(value));
  }

  public static Filter compareVersion(String value) {
    return compareVersion(Operator.EQ, value);
  }

  public static Filter compareVersion(Operator op, String value) {
    Assert.notNull(op);
    DateTime time = DateTime.parse(value);

    if (time == null) {
      LogUtils.warning(LogUtils.getDefaultLogger(), "Not a DATETIME value:", value);
      return null;
    }
    return new VersionFilter(op, time.getTime());
  }

  public static Filter compareWithColumn(String leftColumn, Operator op, String rightColumn) {
    Assert.notEmpty(leftColumn);
    Assert.notNull(op);
    Assert.notEmpty(rightColumn);
    return new ColumnColumnFilter(leftColumn, op, rightColumn);
  }

  public static Filter compareWithColumn(IsColumn left, Operator op, IsColumn right) {
    Assert.noNulls(left, op, right);
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
    return compareWithColumn(leftColumn, op, rightColumn);
  }

  public static Filter compareWithValue(String column, Operator op, Value value) {
    Assert.notEmpty(column);
    Assert.noNulls(op, value);
    return new ColumnValueFilter(column, op, value);
  }

  public static Filter compareWithValue(IsColumn column, Operator op, String value) {
    Assert.noNulls(column, op);
    Assert.notEmpty(value);

    if (ValueType.isNumeric(column.getType()) && !BeeUtils.isDouble(value)) {
      LogUtils.warning(LogUtils.getDefaultLogger(), "Not a numeric value: " + value);
      return null;
    }
    return compareWithValue(column.getId(), op, Value.parseValue(column.getType(), value, true));
  }

  public static Filter isEqual(String column, Value value) {
    return compareWithValue(column, Operator.EQ, value);
  }

  public static Filter isLess(String column, Value value) {
    return compareWithValue(column, Operator.LT, value);
  }

  public static Filter isLessEqual(String column, Value value) {
    return compareWithValue(column, Operator.LE, value);
  }

  public static Filter isMore(String column, Value value) {
    return compareWithValue(column, Operator.GT, value);
  }

  public static Filter isMoreEqual(String column, Value value) {
    return compareWithValue(column, Operator.GE, value);
  }

  public static Filter isNotEqual(String column, Value value) {
    return compareWithValue(column, Operator.NE, value);
  }

  private String column;
  private Operator operator;
  private Object value;

  protected ComparisonFilter() {
    super();
  }

  protected ComparisonFilter(String column, Operator operator, Object value) {
    this.column = column;
    this.operator = operator;
    this.value = value;
  }

  @Override
  public void deserialize(String s) {
    setSafe();
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String xpr = arr[i];

      switch (member) {
        case COLUMN:
          column = xpr;
          break;
        case OPERATOR:
          operator = Operator.valueOf(xpr);
          break;
        case VALUE:
          value = restoreValue(xpr);
          break;
      }
    }
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

    if (!column.equals(other.column)) {
      return false;
    }
    if (!operator.equals(other.operator)) {
      return false;
    }
    if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  public String getColumn() {
    return column;
  }

  public Operator getOperator() {
    return operator;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + column.hashCode();
    result = prime * result + operator.hashCode();
    result = prime * result + value.hashCode();
    return result;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case COLUMN:
          arr[i++] = column;
          break;
        case OPERATOR:
          arr[i++] = operator;
          break;
        case VALUE:
          arr[i++] = value;
          break;
      }
    }
    return super.serialize(arr);
  }

  @Override
  public String toString() {
    return BeeUtils.concat(0, column, operator.toTextString(), value);
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

  protected abstract Object restoreValue(String s);

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
          if (String.valueOf(c).matches("\\W")) {
            regexp.append("\\");
          }
          regexp.append(c);
        }
      }
    }
    return s1.matches(regexp.toString());
  }
}