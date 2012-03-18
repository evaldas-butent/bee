package com.butent.bee.server.sql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlConstants.SqlFunction;
import com.butent.bee.server.sql.SqlConstants.SqlKeyword;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Contains various utility SQL statement related functions like joining by comparisons, creating
 * and dropping keys etc.
 */

public class SqlUtils {

  public static IsExpression aggregate(SqlFunction fnc, IsExpression expr) {
    Map<String, Object> params = Maps.newHashMap();
    params.put("expression", expr);
    return new FunctionExpression(fnc, params);
  }

  public static HasConditions and(IsCondition... conditions) {
    return CompoundCondition.and(conditions);
  }

  public static <T> IsExpression bitAnd(IsExpression expr, T value) {
    return new FunctionExpression(SqlFunction.BITAND,
        ImmutableMap.of("expression", expr, "value", value));
  }

  public static <T> IsExpression bitAnd(String source, String field, T value) {
    return bitAnd(field(source, field), value);
  }

  public static IsExpression cast(IsExpression expr, SqlDataType type, int precision, int scale) {
    return new FunctionExpression(SqlFunction.CAST,
        ImmutableMap.of("expression", expr, "type", type, "precision", precision, "scale", scale));
  }

  public static IsCondition compare(IsExpression expr, Operator op, IsExpression value) {
    return new ComparisonCondition(op, expr, value);
  }

  public static IsExpression constant(Object constant) {
    return new ConstantExpression(Value.getValue(constant));
  }

  public static IsCondition contains(IsExpression expr, String value) {
    Assert.notEmpty(value);
    return compare(expr, Operator.CONTAINS, constant(value));
  }

  public static IsCondition contains(String source, String field, String value) {
    return contains(field(source, field), value);
  }

  public static IsQuery createForeignKey(String table, String name, String field,
      String refTable, String refField, SqlKeyword cascade) {

    Map<String, Object> params = getConstraintMap(SqlKeyword.FOREIGN_KEY, table, name, field);
    params.put("refTable", name(refTable));
    params.put("refFields", name(refField));
    params.put("cascade", cascade);

    return new SqlCommand(SqlKeyword.ADD_CONSTRAINT, params);
  }

  public static IsQuery createIndex(boolean isUnique, String table, String name, String... fields) {
    Map<String, Object> params = getConstraintMap(null, table, name, fields);
    params.put("isUnique", isUnique);

    return new SqlCommand(SqlKeyword.CREATE_INDEX, params);
  }

  public static IsQuery createPrimaryKey(String table, String name, String... fields) {
    return new SqlCommand(SqlKeyword.ADD_CONSTRAINT,
        getConstraintMap(SqlKeyword.PRIMARY_KEY, table, name, fields));
  }

  public static IsQuery[] createTrigger(String table, String name, Object content, String timing,
      String event, String scope) {

    Map<String, Object> params = Maps.newHashMap();
    params.put("table", name(table));
    params.put("name", name(name));
    params.put("content", content);
    params.put("timing", timing);
    params.put("event", event);
    params.put("scope", scope);

    IsQuery function = new SqlCommand(SqlKeyword.CREATE_TRIGGER_FUNCTION, params);
    IsQuery trigger = new SqlCommand(SqlKeyword.CREATE_TRIGGER, params);

    if (BeeUtils.isEmpty(function.getQuery())) {
      return new IsQuery[] {trigger};
    } else {
      return new IsQuery[] {function, trigger};
    }
  }

  public static IsQuery dbFields(String dbName, String dbSchema, String table) {
    Map<String, Object> params = Maps.newHashMap();
    params.put("dbName", dbName);
    params.put("dbSchema", dbSchema);
    params.put("table", table);

    return new SqlCommand(SqlKeyword.DB_FIELDS, params);
  }

  public static IsQuery dbForeignKeys(String dbName, String dbSchema, String table, String refTable) {
    Map<String, Object> params = Maps.newHashMap();
    params.put("dbName", dbName);
    params.put("dbSchema", dbSchema);
    params.put("table", table);
    params.put("refTable", refTable);

    return new SqlCommand(SqlKeyword.DB_FOREIGNKEYS, params);
  }

  public static IsQuery dbIndexes(String dbName, String dbSchema, String table) {
    Map<String, Object> params = Maps.newHashMap();
    params.put("dbName", dbName);
    params.put("dbSchema", dbSchema);
    params.put("table", table);

    return new SqlCommand(SqlKeyword.DB_INDEXES, params);
  }

  public static IsQuery dbKeys(String dbName, String dbSchema, String table, SqlKeyword... types) {
    Map<String, Object> params = Maps.newHashMap();
    params.put("dbName", dbName);
    params.put("dbSchema", dbSchema);
    params.put("table", table);
    params.put("keyTypes", types);

    return new SqlCommand(SqlKeyword.DB_KEYS, params);
  }

  public static IsQuery dbName() {
    return new SqlCommand(SqlKeyword.DB_NAME, null);
  }

  public static IsQuery dbSchema() {
    return new SqlCommand(SqlKeyword.DB_SCHEMA, null);
  }

  public static IsQuery dbTables(String dbName, String dbSchema, String table) {
    Map<String, Object> params = Maps.newHashMap();
    params.put("dbName", dbName);
    params.put("dbSchema", dbSchema);
    params.put("table", table);

    return new SqlCommand(SqlKeyword.DB_TABLES, params);
  }

  public static IsQuery dbTriggers(String dbName, String dbSchema, String table) {
    Map<String, Object> params = Maps.newHashMap();
    params.put("dbName", dbName);
    params.put("dbSchema", dbSchema);
    params.put("table", table);

    return new SqlCommand(SqlKeyword.DB_TRIGGERS, params);
  }

  public static IsExpression divide(IsExpression... members) {
    Assert.minLength(members, 2);
    Assert.noNulls((Object[]) members);
    return new FunctionExpression(SqlFunction.DIVIDE, getMemberMap((Object[]) members));
  }

  public static IsQuery dropForeignKey(String table, String name) {
    return new SqlCommand(SqlKeyword.DROP_FOREIGNKEY,
        ImmutableMap.of("table", (Object) name(table), "name", name(name)));
  }

  public static IsQuery dropTable(String table) {
    return new SqlCommand(SqlKeyword.DROP_TABLE, ImmutableMap.of("table", (Object) name(table)));
  }

  public static IsCondition endsWith(IsExpression expr, String value) {
    Assert.notEmpty(value);
    return compare(expr, Operator.ENDS, constant(value));
  }

  public static IsCondition endsWith(String source, String field, String value) {
    return endsWith(field(source, field), value);
  }

  public static IsCondition equal(IsExpression expr, Object value) {
    return compare(expr, Operator.EQ, constant(value));
  }

  public static IsCondition equal(String source, String field, Object value) {
    return equal(field(source, field), value);
  }

  public static IsExpression expression(Object... members) {
    Assert.minLength(members, 1);
    Assert.noNulls(members);
    return new FunctionExpression(SqlFunction.BULK, getMemberMap(members));
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
    Assert.notNull(query);
    return new ComparisonCondition(Operator.IN, field(src, fld), query);
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
    HasConditions cond = or();

    for (Object value : values) {
      cond.add(equal(expr, value));
    }
    return cond;
  }

  public static IsCondition inList(String source, String field, Object... values) {
    return inList(field(source, field), values);
  }

  public static IsCondition isNull(IsExpression expr) {
    return new ComparisonCondition(Operator.IS_NULL, expr);
  }

  public static IsCondition isNull(String src, String fld) {
    return isNull(field(src, fld));
  }

  public static IsCondition join(String src1, String fld1, String src2, String fld2) {
    return compare(field(src1, fld1), Operator.EQ, field(src2, fld2));
  }

  public static IsCondition joinLess(String src1, String fld1, String src2, String fld2) {
    return compare(field(src1, fld1), Operator.LT, field(src2, fld2));
  }

  public static IsCondition joinLessEqual(String src1, String fld1, String src2, String fld2) {
    return compare(field(src1, fld1), Operator.LE, field(src2, fld2));
  }

  public static IsCondition joinMore(String src1, String fld1, String src2, String fld2) {
    return compare(field(src1, fld1), Operator.GT, field(src2, fld2));
  }

  public static IsCondition joinMoreEqual(String src1, String fld1, String src2, String fld2) {
    return compare(field(src1, fld1), Operator.GE, field(src2, fld2));
  }

  public static IsCondition joinNotEqual(String src1, String fld1, String src2, String fld2) {
    return compare(field(src1, fld1), Operator.NE, field(src2, fld2));
  }

  public static IsCondition joinUsing(String src1, String src2, String... flds) {
    Assert.minLength(flds, 1);

    IsCondition cond = null;

    if (flds.length > 1) {
      HasConditions cb = and();

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
    return compare(expr, Operator.LT, constant(value));
  }

  public static IsCondition less(String source, String field, Object value) {
    return less(field(source, field), value);
  }

  public static IsCondition lessEqual(IsExpression expr, Object value) {
    return compare(expr, Operator.LE, constant(value));
  }

  public static IsCondition lessEqual(String source, String field, Object value) {
    return lessEqual(field(source, field), value);
  }

  public static IsCondition matches(IsExpression expr, String value) {
    Assert.notEmpty(value);
    return compare(expr, Operator.MATCHES, constant(value));
  }

  public static IsCondition matches(String source, String field, String value) {
    return matches(field(source, field), value);
  }

  public static IsExpression minus(IsExpression... members) {
    Assert.minLength(members, 2);
    Assert.noNulls((Object[]) members);
    return new FunctionExpression(SqlFunction.MINUS, getMemberMap((Object[]) members));
  }

  public static IsCondition more(IsExpression expr, Object value) {
    return compare(expr, Operator.GT, constant(value));
  }

  public static IsCondition more(String source, String field, Object value) {
    return more(field(source, field), value);
  }

  public static IsCondition moreEqual(IsExpression expr, Object value) {
    return compare(expr, Operator.GE, constant(value));
  }

  public static IsCondition moreEqual(String source, String field, Object value) {
    return moreEqual(field(source, field), value);
  }

  public static IsExpression multiply(IsExpression... members) {
    Assert.minLength(members, 2);
    Assert.noNulls((Object[]) members);
    return new FunctionExpression(SqlFunction.MULTIPLY, getMemberMap((Object[]) members));
  }

  public static IsExpression name(String name) {
    return new NameExpression(name);
  }

  public static IsCondition not(IsCondition condition) {
    return new NegationCondition(condition);
  }

  public static IsCondition notEqual(IsExpression expr, Object value) {
    return compare(expr, Operator.NE, constant(value));
  }

  public static IsCondition notEqual(String source, String field, Object value) {
    return notEqual(field(source, field), value);
  }

  public static IsCondition notNull(IsExpression expr) {
    return new ComparisonCondition(Operator.NOT_NULL, expr);
  }

  public static IsCondition notNull(String src, String fld) {
    return notNull(field(src, fld));
  }

  public static HasConditions or(IsCondition... conditions) {
    return CompoundCondition.or(conditions);
  }

  public static IsExpression plus(IsExpression... members) {
    Assert.minLength(members, 2);
    Assert.noNulls((Object[]) members);
    return new FunctionExpression(SqlFunction.PLUS, getMemberMap((Object[]) members));
  }

  public static IsQuery renameTable(String from, String to) {
    return new SqlCommand(SqlKeyword.RENAME_TABLE,
        ImmutableMap.of("nameFrom", (Object) name(from), "nameTo", name(to)));
  }

  public static IsExpression sqlCase(IsExpression expr, Object... pairs) {
    Assert.noNulls(expr, pairs);
    Assert.parameterCount(pairs.length, 3);
    Assert.notEmpty(pairs.length % 2);

    Map<String, Object> params = Maps.newHashMap();
    params.put("expression", expr);
    params.put("caseElse", pairs[pairs.length - 1]);

    for (int i = 0; i < (pairs.length - 1) / 2; i++) {
      params.put("case" + i, pairs[i * 2]);
      params.put("value" + i, pairs[i * 2 + 1]);
    }
    return new FunctionExpression(SqlFunction.CASE, params);
  }

  public static IsCondition sqlFalse() {
    return equal(constant(1), 0);
  }

  public static IsExpression sqlIf(IsCondition cond, Object ifTrue, Object ifFalse) {
    return new FunctionExpression(SqlFunction.IF,
        ImmutableMap.of("condition", cond, "ifTrue", ifTrue, "ifFalse", ifFalse));
  }

  public static IsCondition sqlTrue() {
    return equal(constant(1), 1);
  }

  public static IsCondition startsWith(IsExpression expr, String value) {
    Assert.notEmpty(value);
    return compare(expr, Operator.STARTS, constant(value));
  }

  public static IsCondition startsWith(String source, String field, String value) {
    return startsWith(field(source, field), value);
  }

  public static String temporaryName() {
    String tmp = "tmp_" + uniqueName();
    return temporaryName(tmp);
  }

  public static String temporaryName(String tmp) {
    if (BeeUtils.isEmpty(tmp)) {
      return temporaryName();
    }
    return new SqlCommand(SqlKeyword.TEMPORARY_NAME, ImmutableMap.of("name", (Object) tmp))
        .getQuery();
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

  private static Map<String, Object> getConstraintMap(SqlKeyword type, String table, String name,
      String... fields) {
    IsExpression fldList;

    if (BeeUtils.isEmpty(fields)) {
      fldList = name(name);
    } else {
      List<Object> flds = Lists.newArrayList();
      for (String fld : fields) {
        if (!BeeUtils.isEmpty(flds)) {
          flds.add(", ");
        }
        flds.add(name(fld));
      }
      fldList = expression(flds.toArray());
    }
    Map<String, Object> params = Maps.newHashMap();
    params.put("table", name(table));
    params.put("name", name(name));
    params.put("type", type);
    params.put("fields", fldList);
    return params;
  }

  private static Map<String, Object> getMemberMap(Object... members) {
    Map<String, Object> params = Maps.newHashMap();

    if (!BeeUtils.isEmpty(members)) {
      for (int i = 0; i < members.length; i++) {
        params.put("member" + i, members[i]);
      }
    }
    return params;
  }

  private SqlUtils() {
  }
}
