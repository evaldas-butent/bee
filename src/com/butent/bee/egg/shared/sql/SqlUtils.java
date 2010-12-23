package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlUtils {

  private static final String EQUAL = "=";
  private static final String IN = " IN ";
  private static final String LESS = "<";
  private static final String LESS_EQUAL = "<=";
  private static final String LIKE = " LIKE ";
  private static final String MORE = ">";
  private static final String MORE_EQUAL = ">=";
  private static final String NOT_EQUAL = "<>";

  public static Conditions and(IsCondition... conditions) {
    Conditions cb = new AndConditions();
    cb.add(conditions);
    return cb;
  }

  public static IsExpression constant(Object constant) {
    return new ConstantExpression(constant);
  }

  public static IsCondition contains(IsExpression expr, Object value) {
    return new JoinCondition(expr, LIKE, constant("%" + value + "%"));
  }

  public static IsQuery createForeignKey(String table, String name, String field,
      String refTable, String refField, Keywords action) {

    List<Object> params = new ArrayList<Object>();
    params.add(name(field));
    params.add(name(refTable));
    params.add(name(refField));
    params.add(action);

    return createConstraint(table, name, Keywords.FOREIGNKEY, params.toArray());
  }

  public static IsQuery createIndex(String table, String name, String... fields) {
    return createIndex(false, table, name, fields);
  }

  public static IsQuery createPrimaryKey(String table, String name, String... fields) {
    List<Object> params = new ArrayList<Object>();

    if (BeeUtils.isEmpty(fields)) {
      params.add(name(name));
    } else {
      for (String fld : fields) {
        params.add(name(fld));
      }
    }
    return createConstraint(table, name, Keywords.PRIMARYKEY, params.toArray());
  }

  public static IsQuery createUniqueIndex(String table, String name, String... fields) {
    return createIndex(true, table, name, fields);
  }

  public static IsQuery dbForeignKeys(String dbName, String dbSchema, String table, String refTable) {
    return new SqlCommand(Keywords.DB_FOREIGNKEYS, dbName, dbSchema, table, refTable);
  }

  public static IsQuery dbName() {
    return new SqlCommand(Keywords.DB_NAME);
  }

  public static IsQuery dbSchema() {
    return new SqlCommand(Keywords.DB_SCHEMA);
  }

  public static IsQuery dbTables(String dbName, String dbSchema, String table) {
    return new SqlCommand(Keywords.DB_TABLES, dbName, dbSchema, table);
  }

  public static IsQuery dropForeignKey(String table, String name) {
    return new SqlCommand(Keywords.DROP_FOREIGNKEY, name(table), name(name));
  }

  public static IsQuery dropTable(String table) {
    return new SqlCommand(Keywords.DROP_TABLE, name(table));
  }

  public static IsCondition equal(IsExpression expr, Object value) {
    return new JoinCondition(expr, EQUAL, constant(value));
  }

  public static IsCondition equal(String source, String field, Object value) {
    return equal(field(source, field), value);
  }

  public static IsExpression expression(Object... expr) {
    return new ComplexExpression(expr);
  }

  public static IsExpression field(String source, String field) {
    Assert.notEmpty(source);
    Assert.notEmpty(field);
    return name(BeeUtils.concat(".", source, field));
  }

  public static IsExpression[] fields(String source, String... fields) {
    Assert.arrayLengthMin(fields, 1);

    int len = BeeUtils.arrayLength(fields);
    IsExpression[] list = new IsExpression[len];

    for (int i = 0; i < len; i++) {
      list[i] = field(source, fields[i]);
    }
    return list;
  }

  public static IsCondition in(String src, String fld, SqlSelect query) {
    return new JoinCondition(field(src, fld), IN, query);
  }

  public static IsCondition in(String src, String fld, String dst, String dFld, IsCondition clause) {
    SqlSelect query = new SqlSelect();
    query.addDistinct(dst, dFld).addFrom(dst).setWhere(clause);

    return in(src, fld, query);
  }

  public static IsCondition inList(IsExpression expr, Object... values) {
    Assert.arrayLengthMin(values, 1);

    Conditions cond = new OrConditions();

    for (Object value : values) {
      cond.add(equal(expr, value));
    }
    return cond;
  }

  public static IsCondition inList(String source, String field, Object... values) {
    return inList(field(source, field), values);
  }

  public static IsCondition join(String src1, String fld1, String src2, String fld2) {
    return new JoinCondition(field(src1, fld1), EQUAL, field(src2, fld2));
  }

  public static IsCondition joinLess(String src1, String fld1, String src2, String fld2) {
    return new JoinCondition(field(src1, fld1), LESS, field(src2, fld2));
  }

  public static IsCondition joinLessEqual(String src1, String fld1, String src2, String fld2) {
    return new JoinCondition(field(src1, fld1), LESS_EQUAL, field(src2, fld2));
  }

  public static IsCondition joinMore(String src1, String fld1, String src2, String fld2) {
    return new JoinCondition(field(src1, fld1), MORE, field(src2, fld2));
  }

  public static IsCondition joinMoreEqual(String src1, String fld1, String src2, String fld2) {
    return new JoinCondition(field(src1, fld1), MORE_EQUAL, field(src2, fld2));
  }

  public static IsCondition joinMulti(String src1, String src2, String... flds) {
    Assert.arrayLengthMin(flds, 1);

    IsCondition cond = null;

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

  public static IsCondition joinNotEqual(String src1, String fld1, String src2, String fld2) {
    return new JoinCondition(field(src1, fld1), NOT_EQUAL, field(src2, fld2));
  }

  public static IsCondition less(IsExpression expr, Object value) {
    return new JoinCondition(expr, LESS, constant(value));
  }

  public static IsCondition less(String source, String field, Object value) {
    return less(field(source, field), value);
  }

  public static IsCondition lessEqual(IsExpression expr, Object value) {
    return new JoinCondition(expr, LESS_EQUAL, constant(value));
  }

  public static IsCondition lessEqual(String source, String field, Object value) {
    return lessEqual(field(source, field), value);
  }

  public static IsCondition more(IsExpression expr, Object value) {
    return new JoinCondition(expr, MORE, constant(value));
  }

  public static IsCondition more(String source, String field, Object value) {
    return more(field(source, field), value);
  }

  public static IsCondition moreEqual(IsExpression expr, Object value) {
    return new JoinCondition(expr, MORE_EQUAL, constant(value));
  }

  public static IsCondition moreEqual(String source, String field, Object value) {
    return moreEqual(field(source, field), value);
  }

  public static IsExpression name(String name) {
    return new NameExpression(name);
  }

  public static IsCondition notEqual(IsExpression expr, Object value) {
    return new JoinCondition(expr, NOT_EQUAL, constant(value));
  }

  public static IsCondition notEqual(String source, String field, Object value) {
    return notEqual(field(source, field), value);
  }

  public static Conditions or(IsCondition... conditions) {
    Conditions cb = new OrConditions();
    cb.add(conditions);
    return cb;
  }

  public static IsCondition sqlFalse() {
    return equal(constant(1), 0);
  }

  public static IsCondition sqlTrue() {
    return equal(constant(1), 1);
  }

  public static String temporaryName() {
    String tmp = "tmp_" + uniqueName();
    return temporaryName(tmp);
  }

  public static String temporaryName(String tmp) {
    if (BeeUtils.isEmpty(tmp)) {
      return temporaryName();
    }
    return new SqlCommand(Keywords.TEMPORARY_NAME, tmp).getQuery();
  }

  public static String uniqueName() {
    return BeeUtils.randomString(3, 3, 'a', 'z');
  }

  static void addParams(List<Object> paramList, List<Object> params) {
    if (!BeeUtils.isEmpty(params)) {
      if (BeeUtils.isEmpty(paramList)) {
        paramList = params;
      } else {
        paramList.addAll(params);
      }
    }
  }

  private static IsQuery createConstraint(String table, String name, Keywords type,
      Object... params) {
    List<Object> flds = new ArrayList<Object>();
    flds.add(name(table));
    flds.add(name(name));
    flds.add(type);

    for (Object prm : params) {
      flds.add(prm);
    }
    return new SqlCommand(Keywords.ADD_CONSTRAINT, flds.toArray());
  }

  private static IsQuery createIndex(boolean unique, String table, String name, String... fields) {
    List<Object> flds = new ArrayList<Object>();
    flds.add(unique);
    flds.add(name(table));
    flds.add(name(name));

    if (BeeUtils.isEmpty(fields)) {
      flds.add(name(name));
    } else {
      for (String fld : fields) {
        flds.add(name(fld));
      }
    }
    return new SqlCommand(Keywords.CREATE_INDEX, flds.toArray());
  }
}
