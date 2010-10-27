package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public abstract class SqlBuilder {

  protected abstract String sqlQuote(String value);

  public String sqlTransform(Object x) {
    String s = BeeUtils.transform(x);

    if (x instanceof CharSequence) {
      s = "'" + s.replaceAll("'", "\\\\'") + "'";
    }
    return s;
  }

  String getDelete(SqlDelete sd, boolean paramMode) {
    Assert.notNull(sd);
    Assert.state(!sd.isEmpty());

    StringBuilder query = new StringBuilder("DELETE ");

    IsFrom target = sd.getTarget();

    if (!BeeUtils.isEmpty(target)) {
      query.append(" FROM ").append(target.getSqlString(this, paramMode));
    }

    List<IsFrom> fromList = sd.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getJoinMode()).append(
            from.getSqlString(this, paramMode));
      }
    }

    IsCondition whereClause = sd.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      query.append(" WHERE ").append(whereClause.getSqlString(this, paramMode));
    }
    return query.toString();
  }

  String getInsert(SqlInsert si, boolean paramMode) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());
    // TODO Auto-generated method stub
    return null;
  }

  String getQuery(SqlSelect ss, boolean paramMode) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    StringBuilder query = new StringBuilder("SELECT ");

    List<Object[]> fieldList = ss.getFields();

    if (!BeeUtils.isEmpty(fieldList)) {
      for (int i = 0; i < fieldList.size(); i++) {
        Object[] fldEntry = fieldList.get(i);

        if (i > 0) {
          query.append(", ");
        }
        IsExpression field = (IsExpression) fldEntry[SqlSelect.FIELD_EXPR];
        query.append(field.getSqlString(this, paramMode));

        String alias = (String) fldEntry[SqlSelect.FIELD_ALIAS];

        if (!BeeUtils.isEmpty(alias)) {
          query.append(" AS ").append(sqlQuote(alias));
        }
      }
    }

    List<IsFrom> fromList = ss.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getJoinMode()).append(
            from.getSqlString(this, paramMode));
      }
    }

    IsCondition whereClause = ss.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      query.append(" WHERE ").append(whereClause.getSqlString(this, paramMode));
    }

    List<IsExpression> groupList = ss.getGroupBy();

    if (!BeeUtils.isEmpty(groupList)) {
      query.append(" GROUP BY ");

      for (int i = 0; i < groupList.size(); i++) {
        String group = groupList.get(i).getSqlString(this, paramMode);
        if (i > 0) {
          query.append(", ");
        }
        query.append(group);
      }
    }

    List<Object[]> orderList = ss.getOrderBy();

    if (!BeeUtils.isEmpty(orderList)) {
      query.append(" ORDER BY ");

      for (int i = 0; i < orderList.size(); i++) {
        Object[] orderEntry = orderList.get(i);
        if (i > 0) {
          query.append(", ");
        }
        IsExpression order = (IsExpression) orderEntry[SqlSelect.ORDER_EXPR];
        query.append(order.getSqlString(this, paramMode));

        if ((Boolean) orderEntry[SqlSelect.ORDER_DESC]) {
          query.append(" DESC");
        }
      }
    }

    IsCondition havingClause = ss.getHaving();

    if (!BeeUtils.isEmpty(havingClause)) {
      query.append(" HAVING ").append(
          havingClause.getSqlString(this, paramMode));
    }

    List<SqlSelect> unionList = ss.getUnion();

    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        query.append(ss.getUnionMode()).append(
            union.getSqlString(this, paramMode));
      }
    }
    return query.toString();
  }

  String getUpdate(SqlUpdate su, boolean paramMode) {
    Assert.notNull(su);
    Assert.state(!su.isEmpty());

    StringBuilder query = new StringBuilder("UPDATE ");

    IsFrom target = su.getTarget();

    if (!BeeUtils.isEmpty(target)) {
      query.append(target.getSqlString(this, paramMode));
    }

    List<IsExpression[]> fieldList = su.getFields();

    if (!BeeUtils.isEmpty(fieldList)) {
      query.append(" SET ");

      for (int i = 0; i < fieldList.size(); i++) {
        IsExpression[] fldEntry = fieldList.get(i);

        if (i > 0) {
          query.append(", ");
        }
        IsExpression field = fldEntry[SqlUpdate.FIELD];
        query.append(field.getSqlString(this, paramMode));

        IsExpression value = fldEntry[SqlUpdate.VALUE];
        query.append(" = ").append(value.getSqlString(this, paramMode));
      }
    }

    List<IsFrom> fromList = su.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getJoinMode()).append(
            from.getSqlString(this, paramMode));
      }
    }

    IsCondition whereClause = su.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      query.append(" WHERE ").append(whereClause.getSqlString(this, paramMode));
    }
    return query.toString();
  }
}
