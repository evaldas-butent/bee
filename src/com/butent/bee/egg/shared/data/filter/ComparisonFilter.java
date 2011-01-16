package com.butent.bee.egg.shared.data.filter;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.utils.StringTokenizer;

public abstract class ComparisonFilter extends RowFilter {
  public static enum Operator {
    EQ("=", true),
    NE("!=", true),
    LT("<", true),
    GT(">", true),
    LE("<=", true),
    GE(">=", true),
    CONTAINS("CONTAINS", false),
    STARTS_WITH("STARTS WITH", false),
    ENDS_WITH("ENDS WITH", false),
    MATCHES("MATCHES", false),
    LIKE("LIKE", false);

    private String queryStringForm;

    private boolean requiresEqualTypes;

    Operator(String queryStringForm, boolean requiresEqualTypes) {
      this.queryStringForm = queryStringForm;
      this.requiresEqualTypes = requiresEqualTypes;
    }

    public boolean areEqualTypesRequired() {
      return requiresEqualTypes;
    }
    public String toQueryString() {
      return queryStringForm;
    }
  }

  protected Operator operator;

  protected ComparisonFilter(Operator operator) {
    this.operator = operator;
  }

  @Override
  public abstract boolean equals(Object obj);

  public Operator getOperator() {
    return operator;
  }

  @Override
  public abstract int hashCode();

  protected boolean isOperatorMatch(Value v1, Value v2) {
    if (operator.areEqualTypesRequired()) {
      if (!v1.getType().equals(v2.getType())) {
        return false;
      }
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
      case CONTAINS:
        return v1.toString().contains(v2.toString());
      case STARTS_WITH:
        return v1.toString().startsWith(v2.toString());
      case ENDS_WITH:
        return v1.toString().endsWith(v2.toString());
      case MATCHES:
        return v1.toString().matches(v2.toString());
      case LIKE:
        return isLike(v1.toString(), v2.toString());
    }
    Assert.untouchable();
    return false;
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
        regexp.append("\\Q" + s + "\\E"); // TODO Pattern.quote;
      }
    }
    return s1.matches(regexp.toString());
  }
}
