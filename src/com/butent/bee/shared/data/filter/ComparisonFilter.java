package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

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
          arr[i++] = getColumn();
          break;
        case OPERATOR:
          arr[i++] = getOperator();
          break;
        case VALUE:
          arr[i++] = getValue();
          break;
      }
    }
    return super.serialize(arr);
  }

  @Override
  public String toString() {
    return BeeUtils.join(BeeConst.STRING_EMPTY, column, operator.toTextString(), value);
  }

  protected boolean isOperatorMatch(Value v1, Value v2) {
    if (v1.isNull() || v2.isNull()) {
      return false;
    }
    switch (operator) {
      case EQ:
        return v1.compareTo(v2) == 0;
      case NE:
        return v1.compareTo(v2) != 0;
      case LT:
        return v1.compareTo(v2) < 0;
      case GT:
        return v1.compareTo(v2) > 0;
      case LE:
        return v1.compareTo(v2) <= 0;
      case GE:
        return v1.compareTo(v2) >= 0;
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

  private static boolean isLike(String s1, String s2) {
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