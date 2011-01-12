package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
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
  private boolean unionAllMode = false;
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

  public SqlSelect addExpr(IsExpression expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addField(expr, SqlUtils.name(alias));
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
    Assert.arrayLengthMin(fields, 1);

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
      this.unionList = new ArrayList<SqlSelect>();
    }
    for (SqlSelect un : union) {
      if (!un.isEmpty()) {
        this.unionList.add(un);
      }
    }
    return getReference();
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
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    for (IsExpression[] field : fieldList) {
      SqlUtils.addParams(paramList, field[FIELD_EXPR].getSqlParams());
    }
    for (IsFrom from : getFrom()) {
      SqlUtils.addParams(paramList, from.getSqlParams());
    }
    if (!BeeUtils.isEmpty(whereClause)) {
      SqlUtils.addParams(paramList, whereClause.getSqlParams());
    }
    if (!BeeUtils.isEmpty(groupList)) {
      for (IsExpression group : groupList) {
        SqlUtils.addParams(paramList, group.getSqlParams());
      }
    }
    if (!BeeUtils.isEmpty(orderList)) {
      for (Object[] order : orderList) {
        IsExpression ord = (IsExpression) order[ORDER_FLD];
        SqlUtils.addParams(paramList, ord.getSqlParams());
      }
    }
    if (!BeeUtils.isEmpty(havingClause)) {
      SqlUtils.addParams(paramList, havingClause.getSqlParams());
    }
    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        SqlUtils.addParams(paramList, union.getSqlParams());
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
    return BeeUtils.isEmpty(fieldList) || BeeUtils.isEmpty(getFrom());
  }

  public boolean isUnionAllMode() {
    return unionAllMode;
  }

  public SqlSelect resetFields() {
    if (!BeeUtils.isEmpty(fieldList)) {
      fieldList.clear();
    }
    return getReference();
  }

  public SqlSelect resetOrder() {
    if (!BeeUtils.isEmpty(orderList)) {
      orderList.clear();
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

  private void addField(IsExpression expr, IsExpression alias) {
    IsExpression[] fieldEntry = new IsExpression[2];
    fieldEntry[FIELD_EXPR] = expr;
    fieldEntry[FIELD_ALIAS] = alias;

    if (BeeUtils.isEmpty(fieldList)) {
      fieldList = new ArrayList<IsExpression[]>();
    }
    fieldList.add(fieldEntry);
  }

  private void addGroup(IsExpression... group) {
    if (BeeUtils.isEmpty(groupList)) {
      groupList = new ArrayList<IsExpression>();
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
        orderList = new ArrayList<String[]>();
      }
      orderList.add(orderEntry);
    }
  }
}
