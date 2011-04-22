package com.butent.bee.shared.sql;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.BeeConstants.DataType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class SqlSelect extends HasFrom<SqlSelect> {

  static final int FIELD_EXPR = 0;
  static final int FIELD_ALIAS = 1;

  static final int ORDER_SRC = 0;
  static final int ORDER_FLD = 1;
  static final int ORDER_DESC = 2;

  private List<IsExpression[]> fieldList;
  private IsCondition whereClause;
  private List<IsExpression> groupList;
  private List<String[]> orderList;
  private IsCondition havingClause;
  private List<SqlSelect> unionList;

  private boolean distinctMode = false;
  private boolean unionAllMode = true;
  private int limit = 0;
  private int offset = 0;

  public SqlSelect addAllFields(String source) {
    Assert.notEmpty(source);
    addField(SqlUtils.name(source + ".*"), null);
    return getReference();
  }

  public SqlSelect addAvg(IsExpression expr, String alias) {
    addAggregate("AVG", expr, alias);
    return getReference();
  }

  public SqlSelect addAvg(String source, String field) {
    return addAvg(SqlUtils.field(source, field), field);
  }

  public SqlSelect addAvg(String source, String field, String alias) {
    return addAvg(SqlUtils.field(source, field), alias);
  }

  public SqlSelect addConstant(Object constant, String alias) {
    addExpr(SqlUtils.constant(constant), alias);
    return getReference();
  }

  public SqlSelect addCount(IsExpression expr, String alias) {
    addAggregate("COUNT", expr, alias);
    return getReference();
  }

  public SqlSelect addCount(String alias) {
    return addCount(SqlUtils.expression("*"), alias);
  }

  public SqlSelect addEmptyBoolean(String alias) {
    return addEmptyField(alias, DataType.BOOLEAN, 0, 0);
  }

  public SqlSelect addEmptyChar(String alias, int precision) {
    return addEmptyField(alias, DataType.CHAR, precision, 0);
  }

  public SqlSelect addEmptyDate(String alias) {
    return addEmptyField(alias, DataType.DATE, 0, 0);
  }

  public SqlSelect addEmptyDateTime(String alias) {
    return addEmptyField(alias, DataType.DATETIME, 0, 0);
  }

  public SqlSelect addEmptyDouble(String alias) {
    return addEmptyField(alias, DataType.DOUBLE, 0, 0);
  }

  public SqlSelect addEmptyField(String alias, DataType type, int precision, int scale) {
    Object emptyValue;

    switch (type) {
      case BOOLEAN:
      case INTEGER:
      case LONG:
      case DOUBLE:
      case NUMERIC:
      case DATE:
      case DATETIME:
        emptyValue = 0;
        break;

      case CHAR:
      case STRING:
        emptyValue = "";
        break;

      default:
        Assert.unsupported("Unsupported data type: " + type.name());
        return null;
    }
    addField(SqlUtils.cast(SqlUtils.constant(emptyValue), type, precision, scale), alias);
    return getReference();
  }

  public SqlSelect addEmptyInt(String alias) {
    return addEmptyField(alias, DataType.INTEGER, 0, 0);
  }

  public SqlSelect addEmptyLong(String alias) {
    return addEmptyField(alias, DataType.LONG, 0, 0);
  }

  public SqlSelect addEmptyNumeric(String alias, int precision, int scale) {
    return addEmptyField(alias, DataType.NUMERIC, precision, scale);
  }

  public SqlSelect addEmptyString(String alias, int precision) {
    return addEmptyField(alias, DataType.STRING, precision, 0);
  }

  public SqlSelect addExpr(IsExpression expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addField(expr, alias);
    return getReference();
  }

  public SqlSelect addExpr(String expr, String alias) {
    addExpr(SqlUtils.expression(expr), alias);
    return getReference();
  }

  public SqlSelect addField(String source, String field, String alias) {
    addExpr(SqlUtils.field(source, field), alias);
    return getReference();
  }

  public SqlSelect addFields(String source, String... fields) {
    Assert.minLength(fields, 1);

    for (String fld : fields) {
      addField(SqlUtils.field(source, fld), null);
    }
    return getReference();
  }

  public SqlSelect addGroup(String source, String... fields) {
    addGroup(SqlUtils.fields(source, fields));
    return getReference();
  }

  public SqlSelect addMax(IsExpression expr, String alias) {
    addAggregate("MAX", expr, alias);
    return getReference();
  }

  public SqlSelect addMax(String source, String field) {
    return addMax(SqlUtils.field(source, field), field);
  }

  public SqlSelect addMax(String source, String field, String alias) {
    return addMax(SqlUtils.field(source, field), alias);
  }

  public SqlSelect addMin(IsExpression expr, String alias) {
    addAggregate("MIN", expr, alias);
    return getReference();
  }

  public SqlSelect addMin(String source, String field) {
    return addMin(SqlUtils.field(source, field), field);
  }

  public SqlSelect addMin(String source, String field, String alias) {
    return addMin(SqlUtils.field(source, field), alias);
  }

  public SqlSelect addOrder(String source, String... order) {
    addOrder(false, source, order);
    return getReference();
  }

  public SqlSelect addOrderDesc(String source, String... order) {
    addOrder(true, source, order);
    return getReference();
  }

  public SqlSelect addSum(IsExpression expr, String alias) {
    addAggregate("SUM", expr, alias);
    return getReference();
  }

  public SqlSelect addSum(String source, String field) {
    return addSum(SqlUtils.field(source, field), field);
  }

  public SqlSelect addSum(String source, String field, String alias) {
    return addSum(SqlUtils.field(source, field), alias);
  }

  public SqlSelect addUnion(SqlSelect... union) {
    Assert.noNulls((Object[]) union);

    if (BeeUtils.isEmpty(unionList)) {
      this.unionList = Lists.newArrayList();
    }
    for (SqlSelect un : union) {
      if (!un.isEmpty()) {
        this.unionList.add(un);
      }
    }
    return getReference();
  }

  public SqlSelect copyOf() {
    SqlSelect query = new SqlSelect();

    if (fieldList != null) {
      query.fieldList = Lists.newArrayList(fieldList);
    }
    if (getFrom() != null) {
      query.fromList = Lists.newArrayList(getFrom());
    }
    query.setWhere(whereClause);

    if (groupList != null) {
      query.groupList = Lists.newArrayList(groupList);
    }
    if (orderList != null) {
      query.orderList = Lists.newArrayList(orderList);
    }
    query.setHaving(havingClause);

    if (unionList != null) {
      query.unionList = Lists.newArrayList(unionList);
    }
    query.setDistinctMode(distinctMode);
    query.setUnionAllMode(unionAllMode);
    query.setLimit(limit);
    query.setOffset(offset);

    return query;
  }

  // Getters ----------------------------------------------------------------
  public List<IsExpression[]> getFields() {
    return fieldList;
  }

  public List<IsExpression> getGroupBy() {
    return groupList;
  }

  public IsCondition getHaving() {
    return havingClause;
  }

  public int getLimit() {
    return limit;
  }

  public int getOffset() {
    return offset;
  }

  public List<String[]> getOrderBy() {
    return orderList;
  }

  @Override
  public Collection<String> getSources() {
    Assert.state(!isEmpty());

    Collection<String> sources = super.getSources();

    if (!BeeUtils.isEmpty(whereClause)) {
      sources = SqlUtils.addCollection(sources, whereClause.getSources());
    }
    if (!BeeUtils.isEmpty(havingClause)) {
      sources = SqlUtils.addCollection(sources, havingClause.getSources());
    }
    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        sources = SqlUtils.addCollection(sources, union.getSources());
      }
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    for (IsExpression[] field : fieldList) {
      paramList =
          (List<Object>) SqlUtils.addCollection(paramList, field[FIELD_EXPR].getSqlParams());
    }
    paramList = (List<Object>) SqlUtils.addCollection(paramList, super.getSqlParams());

    if (!BeeUtils.isEmpty(whereClause)) {
      paramList = (List<Object>) SqlUtils.addCollection(paramList, whereClause.getSqlParams());
    }
    if (!BeeUtils.isEmpty(groupList)) {
      for (IsExpression group : groupList) {
        paramList = (List<Object>) SqlUtils.addCollection(paramList, group.getSqlParams());
      }
    }
    if (!BeeUtils.isEmpty(havingClause)) {
      paramList = (List<Object>) SqlUtils.addCollection(paramList, havingClause.getSqlParams());
    }
    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        paramList = (List<Object>) SqlUtils.addCollection(paramList, union.getSqlParams());
      }
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getQuery(this, paramMode);
  }

  public List<SqlSelect> getUnion() {
    return unionList;
  }

  public IsCondition getWhere() {
    return whereClause;
  }

  public boolean isDistinctMode() {
    return distinctMode;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(fieldList) || super.isEmpty();
  }

  public boolean isUnionAllMode() {
    return unionAllMode;
  }

  public SqlSelect reset() {
    resetFields();
    resetGroup();
    resetOrder();
    resetUnion();
    whereClause = null;
    havingClause = null;
    setDistinctMode(false);
    setUnionAllMode(true);
    setOffset(0);
    setLimit(0);
    return getReference();
  }

  public SqlSelect resetFields() {
    if (!BeeUtils.isEmpty(fieldList)) {
      fieldList.clear();
    }
    return getReference();
  }

  public SqlSelect resetGroup() {
    if (!BeeUtils.isEmpty(groupList)) {
      groupList.clear();
    }
    return getReference();
  }

  public SqlSelect resetOrder() {
    if (!BeeUtils.isEmpty(orderList)) {
      orderList.clear();
    }
    return getReference();
  }

  public SqlSelect resetUnion() {
    if (!BeeUtils.isEmpty(unionList)) {
      unionList.clear();
    }
    return getReference();
  }

  public SqlSelect setDistinctMode(boolean distinct) {
    this.distinctMode = distinct;
    return getReference();
  }

  public SqlSelect setHaving(IsCondition having) {
    havingClause = having;
    return getReference();
  }

  public SqlSelect setLimit(int limit) {
    Assert.nonNegative(limit);
    this.limit = limit;
    return getReference();
  }

  public SqlSelect setOffset(int offset) {
    Assert.nonNegative(offset);
    this.offset = offset;
    return getReference();
  }

  public SqlSelect setUnionAllMode(boolean unionAll) {
    unionAllMode = unionAll;
    return getReference();
  }

  public SqlSelect setWhere(IsCondition clause) {
    whereClause = clause;
    return getReference();
  }

  @Override
  protected SqlSelect getReference() {
    return this;
  }

  private void addAggregate(String fnc, IsExpression expr, String alias) {
    Assert.notEmpty(expr);
    addExpr(SqlUtils.expression(fnc, "(", expr, ")"), alias);
  }

  private void addField(IsExpression expr, String alias) {
    IsExpression[] fieldEntry = new IsExpression[2];
    fieldEntry[FIELD_EXPR] = expr;
    fieldEntry[FIELD_ALIAS] = BeeUtils.isEmpty(alias) ? null : SqlUtils.name(alias);

    if (BeeUtils.isEmpty(fieldList)) {
      fieldList = Lists.newArrayList();
    }
    fieldList.add(fieldEntry);
  }

  private void addGroup(IsExpression... group) {
    if (BeeUtils.isEmpty(groupList)) {
      groupList = Lists.newArrayList();
    }
    for (IsExpression grp : group) {
      groupList.add(grp);
    }
  }

  private void addOrder(Boolean desc, String source, String... fields) {
    Assert.notEmpty(source);

    for (String ord : fields) {
      String[] orderEntry = new String[3];
      orderEntry[ORDER_SRC] = source;
      orderEntry[ORDER_FLD] = ord;
      orderEntry[ORDER_DESC] = BeeUtils.isEmpty(desc) ? "" : " DESC";

      if (BeeUtils.isEmpty(orderList)) {
        orderList = Lists.newArrayList();
      }
      orderList.add(orderEntry);
    }
  }
}
