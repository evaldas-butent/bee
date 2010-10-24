package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class SqlUtils {

  private static final String EQUAL = "=";
  private static final String IN = " IN ";
  private static final String LESS = "<";
  private static final String LESS_EQUAL = "<=";
  private static final String LIKE = " LIKE ";
  private static final String MORE = ">";
  private static final String MORE_EQUAL = ">=";
  private static final String NOT_EQUAL = "<>";

  public static Condition and(Condition... conditions) {
    Conditions cb = new AndConditions();
    cb.add(conditions);
    return cb;
  }

  public static ConstantExpression constant(Object constant) {
    return new ConstantExpression(constant);
  }

  public static Condition contains(Expression expr, Object value) {
    return new JoinCondition(expr, LIKE, constant("%" + value + "%"));
  }

  public static Condition equal(Expression expr, Object value) {
    return new JoinCondition(expr, EQUAL, constant(value));
  }

  public static Condition equal(String source, String field, Object value) {
    return equal(field(source, field), value);
  }

  public static Expression field(String source, String field) {
    return new FieldExpression(source, field);
  }

  public static Expression field(String field) {
    return new FieldExpression(field);
  }

  public static Expression[] fields(String source, String... fields) {
    Assert.notEmpty(source);
    Assert.arrayLength(fields, 1);

    int len = BeeUtils.arrayLength(fields);
    Expression[] list = new Expression[len];

    for (int i = 0; i < len; i++) {
      list[i] = new FieldExpression(source, fields[i]);
    }
    return list;
  }

  public static Condition in(String src, String fld, SqlSelect query) {
    return new JoinCondition(field(src, fld), IN, query);
  }

  public static Condition in(String src, String fld, String dst, String dFld,
      Condition clause) {
    SqlSelect query = new SqlSelect();
    query.addDistinct(dst, dFld).addFrom(dst).setWhere(clause);

    return in(src, fld, query);
  }

  public static Condition inList(Expression expr, Object... values) {
    Assert.arrayLength(values, 1);

    Conditions cond = new OrConditions();

    for (Object value : values) {
      cond.add(equal(expr, value));
    }
    return cond;
  }

  public static Condition inList(String source, String field, Object... values) {
    return inList(field(source, field), values);
  }

  public static Condition join(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(field(src1, fld1), EQUAL, field(src2, fld2));
  }

  public static Condition joinLess(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(field(src1, fld1), LESS, field(src2, fld2));
  }

  public static Condition joinLessEqual(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(field(src1, fld1), LESS_EQUAL, field(src2, fld2));
  }

  public static Condition joinMore(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(field(src1, fld1), MORE, field(src2, fld2));
  }

  public static Condition joinMoreEqual(String src1, String fld1, String src2,
      String fld2) {
    return new JoinCondition(field(src1, fld1), MORE_EQUAL, field(src2, fld2));
  }

  public static Condition joinMulti(String src1, String src2, String... flds) {
    Assert.arrayLength(flds, 1);

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
    return new JoinCondition(field(src1, fld1), NOT_EQUAL, field(src2, fld2));
  }

  public static Condition less(Expression expr, Object value) {
    return new JoinCondition(expr, LESS, constant(value));
  }

  public static Condition less(String source, String field, Object value) {
    return less(field(source, field), value);
  }

  public static Condition lessEqual(Expression expr, Object value) {
    return new JoinCondition(expr, LESS_EQUAL, constant(value));
  }

  public static Condition lessEqual(String source, String field, Object value) {
    return lessEqual(field(source, field), value);
  }

  public static Condition more(Expression expr, Object value) {
    return new JoinCondition(expr, MORE, constant(value));
  }

  public static Condition more(String source, String field, Object value) {
    return more(field(source, field), value);
  }

  public static Condition moreEqual(Expression expr, Object value) {
    return new JoinCondition(expr, MORE_EQUAL, constant(value));
  }

  public static Condition moreEqual(String source, String field, Object value) {
    return moreEqual(field(source, field), value);
  }

  public static Condition notEqual(Expression expr, Object value) {
    return new JoinCondition(expr, NOT_EQUAL, constant(value));
  }

  public static Condition notEqual(String source, String field, Object value) {
    return notEqual(field(source, field), value);
  }

  public static Condition or(Condition... conditions) {
    Conditions cb = new OrConditions();
    cb.add(conditions);
    return cb;
  }
}
