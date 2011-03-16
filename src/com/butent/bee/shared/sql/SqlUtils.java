package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.BeeConstants.Keyword;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlUtils {

  private static final String EQUAL = "=";
  private static final String LESS = "<";
  private static final String LESS_EQUAL = "<=";
  private static final String MORE = ">";
  private static final String MORE_EQUAL = ">=";
  private static final String NOT_EQUAL = "<>";
  private static final String LIKE = " LIKE ";
  private static final String IN = " IN ";
  private static final String IS = " IS ";

  public static Conditions and(IsCondition... conditions) {
    Conditions cb = new AndConditions();
    cb.add(conditions);
    return cb;
  }

  public static <T> IsExpression bitAnd(IsExpression expr, T value) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("expression", expr);
    params.put("value", value);

    return expression(new SqlCommand(Keyword.BITAND, params));
  }

  public static <T> IsExpression bitAnd(String source, String field, T value) {
    return bitAnd(field(source, field), value);
  }

  public static IsExpression constant(Object constant) {
    return new ConstantExpression(constant);
  }

  public static IsCondition contains(IsExpression expr, Object value) {
    return new JoinCondition(expr, LIKE, constant("%" + value + "%"));
  }

  public static IsQuery createForeignKey(String table, String name, String field,
      String refTable, String refField, Keyword action) {

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("table", name(table));
    params.put("name", name(name));
    params.put("type", Keyword.FOREIGNKEY);
    params.put("field", name(field));
    params.put("refTable", name(refTable));
    params.put("refField", name(refField));
    params.put("action", action);

    return new SqlCommand(Keyword.ADD_CONSTRAINT, params);
  }

  public static IsQuery createIndex(String table, String name, String... fields) {
    return createIndex(false, table, name, fields);
  }

  public static IsQuery createPrimaryKey(String table, String name, String... fields) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("table", name(table));
    params.put("name", name(name));
    params.put("type", Keyword.PRIMARYKEY);

    if (BeeUtils.isEmpty(fields)) {
      params.put("fields", name(name));
    } else {
      List<IsExpression> flds = new ArrayList<IsExpression>();
      for (String fld : fields) {
        if (!BeeUtils.isEmpty(flds)) {
          flds.add(expression(", "));
        }
        flds.add(name(fld));
      }
      params.put("fields", expression(flds.toArray()));
    }
    return new SqlCommand(Keyword.ADD_CONSTRAINT, params);
  }

  public static IsQuery createUniqueIndex(String table, String name, String... fields) {
    return createIndex(true, table, name, fields);
  }

  public static IsQuery dbForeignKeys(String dbName, String dbSchema, String table, String refTable) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("dbName", dbName);
    params.put("dbSchema", dbSchema);
    params.put("table", table);
    params.put("refTable", refTable);

    return new SqlCommand(Keyword.DB_FOREIGNKEYS, params);
  }

  public static IsQuery dbName() {
    return new SqlCommand(Keyword.DB_NAME, null);
  }

  public static IsQuery dbSchema() {
    return new SqlCommand(Keyword.DB_SCHEMA, null);
  }

  public static IsQuery dbTables(String dbName, String dbSchema, String table) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("dbName", dbName);
    params.put("dbSchema", dbSchema);
    params.put("table", table);

    return new SqlCommand(Keyword.DB_TABLES, params);
  }

  public static IsQuery dropForeignKey(String table, String name) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("table", name(table));
    params.put("name", name(name));

    return new SqlCommand(Keyword.DROP_FOREIGNKEY, params);
  }

  public static IsQuery dropTable(String table) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("table", name(table));

    return new SqlCommand(Keyword.DROP_TABLE, params);
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
    Assert.minLength(fields, 1);

    int len = ArrayUtils.length(fields);
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
    SqlSelect query = new SqlSelect()
      .setDistinctMode(true)
      .addFields(dst, dFld)
      .addFrom(dst)
      .setWhere(clause);

    return in(src, fld, query);
  }

  public static IsCondition inList(IsExpression expr, Object... values) {
    Assert.minLength(values, 1);

    Conditions cond = new OrConditions();

    for (Object value : values) {
      cond.add(equal(expr, value));
    }
    return cond;
  }

  public static IsCondition inList(String source, String field, Object... values) {
    return inList(field(source, field), values);
  }

  public static IsCondition isNotNull(IsExpression expr) {
    return new JoinCondition(expr, IS, expression("NOT NULL"));
  }

  public static IsCondition isNotNull(String src, String fld) {
    return isNotNull(field(src, fld));
  }

  public static IsCondition isNull(IsExpression expr) {
    return new JoinCondition(expr, IS, expression("NULL"));
  }

  public static IsCondition isNull(String src, String fld) {
    return isNull(field(src, fld));
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

  public static IsCondition joinNotEqual(String src1, String fld1, String src2, String fld2) {
    return new JoinCondition(field(src1, fld1), NOT_EQUAL, field(src2, fld2));
  }

  public static IsCondition joinUsing(String src1, String src2, String... flds) {
    Assert.minLength(flds, 1);

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

  public static IsExpression sqlIf(IsCondition cond, Object ifTrue, Object ifFalse) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("condition", cond);
    params.put("ifTrue", ifTrue);
    params.put("ifFalse", ifFalse);
    return expression(new SqlCommand(Keyword.IF, params));
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
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name", tmp);

    return new SqlCommand(Keyword.TEMPORARY_NAME, params).getQuery();
  }

  public static String uniqueName() {
    return BeeUtils.randomString(3, 3, 'a', 'z');
  }

  static <T> Collection<T> addCollection(Collection<T> destination, Collection<T> source) {
    if (!BeeUtils.isEmpty(source)) {
      if (BeeUtils.isEmpty(destination)) {
        destination = source;
      } else {
        destination.addAll(source);
      }
    }
    return destination;
  }

  private static IsQuery createIndex(boolean unique, String table, String name, String... fields) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("unique", unique);
    params.put("table", name(table));
    params.put("name", name(name));

    if (BeeUtils.isEmpty(fields)) {
      params.put("fields", name(name));
    } else {
      List<IsExpression> flds = new ArrayList<IsExpression>();
      for (String fld : fields) {
        if (!BeeUtils.isEmpty(flds)) {
          flds.add(expression(", "));
        }
        flds.add(name(fld));
      }
      params.put("fields", expression(flds.toArray()));
    }
    return new SqlCommand(Keyword.CREATE_INDEX, params);
  }
}
