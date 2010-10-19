package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class SqlUtils {
  public static String SQL_QUOTE = "";
  private static final String EQUAL = "=";
  private static final String NOT_EQUAL = "<>";
  private static final String MORE = ">";
  private static final String MORE_EQUAL = ">=";
  private static final String LESS = "<";
  private static final String LESS_EQUAL = "<=";
  private static final String LIKE = " LIKE ";
  private static final String IN = " IN ";

  public static Conditions and(Condition... conditions) {
    Conditions cb = new AndConditions();
    cb.add(conditions);
    return cb;
  }

  public static Condition contains(String expr, Object value) {
    return new ExpressionCondition(expr, LIKE, "%" + value + "%");
  }

  public static Condition equal(String expr, Object value) {
    return new ExpressionCondition(expr, EQUAL, value);
  }

  public static Condition equal(String source, String field, Object value) {
    return equal(fields(source, field), value);
  }

  public static String fields(String source, String... fields) {
    if (BeeUtils.isEmpty(source) || BeeUtils.isEmpty(fields)) {
      return null;
    }
    String src = sqlQuote(source) + ".";
    StringBuilder sb = new StringBuilder();

    for (String fld : fields) {
      if (!BeeUtils.isEmpty(fld)) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(src).append(sqlQuote(fld));
      }
    }
    return sb.toString();
  }

  public static Condition in(String src, String fld, QueryBuilder query) {
    return new JoinCondition(fields(src, fld), IN, query);
  }

  public static Condition in(String src, String fld, String dst, String dFld,
      Condition clause) {
    QueryBuilder query = new QueryBuilder();
    query.addDistinct(dst, dFld).addFrom(dst).setWhere(clause);

    return in(src, fld, query);
  }

  public static Condition inList(String expr, Object... values) {
    Assert.notEmpty(values);

    Conditions cond = new OrConditions();

    for (Object value : values) {
      cond.add(new ExpressionCondition(expr, EQUAL, value));
    }
    return cond;
  }

  public static Condition inList(String source, String field, Object... values) {
    return inList(fields(source, field), values);
  }

  public static Condition join(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(fields(src1, fld1), EQUAL, fields(src2, fld2));
  }

  public static Condition joinLess(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(fields(src1, fld1), LESS, fields(src2, fld2));
  }

  public static Condition joinLessEqual(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(fields(src1, fld1), LESS_EQUAL, fields(src2, fld2));
  }

  public static Condition joinMore(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(fields(src1, fld1), MORE, fields(src2, fld2));
  }

  public static Condition joinMoreEqual(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(fields(src1, fld1), MORE_EQUAL, fields(src2, fld2));
  }

  public static Condition joinMulti(String src1, String src2, String... flds) {
    Assert.notEmpty(flds);

    Condition cond = null;

    if (flds.length > 1) {
      Conditions cb = new AndConditions();

      for (String fld : flds) {
        cb.add(join(src1, fld, src2, fld));
      }
      cond = cb;

    } else {
      String fld = flds[0];
      cond = join(src1, fld, src2, fld);
    }
    return cond;
  }

  public static Condition joinNotEqual(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(fields(src1, fld1), NOT_EQUAL, fields(src2, fld2));
  }

  public static Condition less(String expr, Object value) {
    return new ExpressionCondition(expr, LESS, value);
  }

  public static Condition less(String source, String field, Object value) {
    return less(fields(source, field), value);
  }

  public static Condition lessEqual(String expr, Object value) {
    return new ExpressionCondition(expr, LESS_EQUAL, value);
  }

  public static Condition lessEqual(String source, String field, Object value) {
    return lessEqual(fields(source, field), value);
  }

  public static Condition more(String expr, Object value) {
    return new ExpressionCondition(expr, MORE, value);
  }

  public static Condition more(String source, String field, Object value) {
    return more(fields(source, field), value);
  }

  public static Condition moreEqual(String expr, Object value) {
    return new ExpressionCondition(expr, MORE_EQUAL, value);
  }

  public static Condition moreEqual(String source, String field, Object value) {
    return moreEqual(fields(source, field), value);
  }

  public static Condition notEqual(String expr, Object value) {
    return new ExpressionCondition(expr, NOT_EQUAL, value);
  }

  public static Condition notEqual(String source, String field, Object value) {
    return notEqual(fields(source, field), value);
  }

  public static Conditions or(Condition... conditions) {
    Conditions cb = new OrConditions();
    cb.add(conditions);
    return cb;
  }

  public static String sqlQuote(String expr) {
    if (BeeUtils.isEmpty(expr)) {
      return null;
    }
    return SQL_QUOTE + expr.trim() + SQL_QUOTE;
  }
}
