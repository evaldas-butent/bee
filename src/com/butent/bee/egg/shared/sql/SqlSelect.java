package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlSelect extends HasFrom<SqlSelect> {

  static final int FIELD_EXPR = 0;
  static final int FIELD_ALIAS = 1;
  static final int ORDER_EXPR = 0;
  static final int ORDER_DESC = 1;

  private List<IsExpression[]> fieldList;
  private IsCondition whereClause;
  private List<IsExpression> groupList;
  private List<Object[]> orderList;
  private IsCondition havingClause;
  private List<SqlSelect> unionList;

  private String unionMode;

  public SqlSelect() {
    setUnionAllMode(true);
  }

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

  public SqlSelect addCount(String alias) {
    return addCount(null, alias);
  }

  public SqlSelect addCount(String expr, String alias) {
    String xpr;
    if (BeeUtils.isEmpty(expr)) {
      xpr = "*";
    } else {
      xpr = expr.trim();
    }
    addAggregate("COUNT", SqlUtils.expression(xpr), alias);
    return getReference();
  }

  public SqlSelect addDistinct(String source, String field) {
    addField(SqlUtils.expression("DISTINCT ", SqlUtils.field(source, field)), null);
    return getReference();
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
    addOrder(false, SqlUtils.fields(source, order));
    return getReference();
  }

  public SqlSelect addOrderDesc(String source, String... order) {
    addOrder(true, SqlUtils.fields(source, order));
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
  public String getFieldName(String src, String fld) {
    Assert.notEmpty(src);
    Assert.notEmpty(fld);
    Assert.state(!isEmpty());

    for (IsExpression[] field : getFields()) {
      String xpr = field[FIELD_EXPR].getValue();

      if (BeeUtils.same(xpr, src + ".*")) {
        return fld;

      } else if (BeeUtils.same(xpr, src + "." + fld)) {
        IsExpression als = field[FIELD_ALIAS];

        if (!BeeUtils.isEmpty(als)) {
          return als.getValue();
        }
        return fld;
      }
    }
    return null;
  }

  public List<IsExpression[]> getFields() {
    return fieldList;
  }

  public List<IsExpression> getGroupBy() {
    return groupList;
  }

  public IsCondition getHaving() {
    return havingClause;
  }

  public String getMainAlias() {
    Assert.state(!isEmpty());

    IsFrom from = getFrom().get(0);
    Object src = from.getSource();

    if (src instanceof String) {
      return BeeUtils.ifString(from.getAlias(), (String) src);
    }
    return null;
  }

  public String getMainSource() {
    Assert.state(!isEmpty());

    Object src = getFrom().get(0).getSource();

    if (src instanceof String) {
      return (String) src;
    }
    return null;
  }

  public List<Object[]> getOrderBy() {
    return orderList;
  }

  public List<String> getSources(String source) {
    Assert.notEmpty(source);

    List<String> lst = new ArrayList<String>();

    if (!BeeUtils.isEmpty(getFrom())) {
      for (IsFrom from : getFrom()) {
        Object src = from.getSource();

        if (src instanceof String && source.equals(src)) {
          String als = from.getAlias();

          if (BeeUtils.isEmpty(als)) {
            als = source;
          }
          lst.add(als);
        }
      }
    }
    return lst;
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
        IsExpression ord = (IsExpression) order[ORDER_EXPR];
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

  public String getUnionMode() {
    return unionMode;
  }

  public IsCondition getWhere() {
    return whereClause;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(fieldList) || BeeUtils.isEmpty(getFrom());
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

  public SqlSelect setHaving(IsCondition having) {
    havingClause = having;
    return getReference();
  }

  public void setUnionAllMode(boolean allMode) {
    unionMode = allMode ? " UNION ALL " : " UNION ";
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

  private void addOrder(Boolean desc, IsExpression... order) {
    for (IsExpression ord : order) {
      Object[] orderEntry = new Object[2];
      orderEntry[ORDER_EXPR] = ord;
      orderEntry[ORDER_DESC] = !BeeUtils.isEmpty(desc);

      if (BeeUtils.isEmpty(orderList)) {
        orderList = new ArrayList<Object[]>();
      }
      orderList.add(orderEntry);
    }
  }
}
