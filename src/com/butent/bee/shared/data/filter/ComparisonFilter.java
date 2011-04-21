package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.StringTokenizer;

public abstract class ComparisonFilter extends Filter {

  public static enum Operator {
    EQ("="),
    NE("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    CONTAINS("$");

    public static Operator getOperator(String op) {
      for (Operator operator : Operator.values()) {
        if (BeeUtils.same(operator.toQueryString(), op)) {
          return operator;
        }
      }
      return null;
    }

    public static String getPattern(boolean multipleChars) {
      StringBuilder sb = new StringBuilder();

      for (Operator operator : Operator.values()) {
        String op = operator.toQueryString();

        if (multipleChars && op.length() > 1 || !multipleChars && op.length() == 1) {
          if (sb.length() > 0) {
            sb.append("|");
          }
          if (CONTAINS == operator) {
            op = "\\" + op;
          }
          sb.append(op);
        }
      }
      return sb.toString();
    }

    private String queryString;

    Operator(String queryString) {
      this.queryString = queryString;
    }

    public String toQueryString() {
      return queryString;
    }
  }

  public static Filter compareWithColumn(String firstColumn, String operator, String secondColumn) {
    return new ColumnColumnFilter(firstColumn, Operator.getOperator(operator), secondColumn);
  }

  public static Filter compareWithValue(String column, String operator, Object value) {
    return new ColumnValueFilter(column, Operator.getOperator(operator), Value.getValue(value));
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

  protected boolean hasLikeCharacters(String xpr) {
    return xpr.matches("^.*[%_].*$");
  }

  protected boolean isOperatorMatch(Value v1, Value v2) {
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
      case CONTAINS:
        String val = v2.toString();

        if (hasLikeCharacters(val)) {
          return isLike(v1.toString(), val);
        }
        return v1.toString().contains(val);
    }
    Assert.untouchable();
    return false;
  }

  protected void setOperator(Operator operator) {
    this.operator = operator;
  }

  private boolean isLike(String s1, String s2) {
    StringTokenizer tokenizer = new StringTokenizer(s2, "%_", true);
    StringBuilder regexp = new StringBuilder();

    while (tokenizer.hasMoreTokens()) {
      String s = tokenizer.nextToken();

      if (s.equals("%")) {
        regexp.append(".*");
      } else if (s.equals("_")) {
        regexp.append(".");
      } else {
        regexp.append(s.replace(".", "\\.").replace("*", "\\*"));
      }
    }
    return s1.matches(regexp.toString());
  }
}