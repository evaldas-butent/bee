package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public abstract class SqlBuilder {

  protected abstract String parseQuotes(String query);

  public String sqlTransform(Object x) {
    String s = BeeUtils.transform(x);
    s = "'" + s.replaceAll("'", "\\\\'") + "'";
    return s;
  }

  String getDelete(SqlDelete sd, boolean paramMode) {
    Assert.notNull(sd);
    Assert.state(!sd.isEmpty());
    // TODO Auto-generated method stub
    Assert.noNulls(paramMode);
    return null;
  }

  String getInsert(SqlInsert si, boolean paramMode) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());
    // TODO Auto-generated method stub
    Assert.noNulls(paramMode);
    return null;
  }

  String getQuery(SqlSelect ss, boolean paramMode) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    StringBuilder query = new StringBuilder("SELECT ");

    List<Map<String, String>> fieldList = ss.getFields();

    for (int i = 0; i < fieldList.size(); i++) {
      Map<String, String> fldMap = fieldList.get(i);

      if (i > 0) {
        query.append(", ");
      }
      String field = fldMap.get("field");
      query.append(field);
      String alias = fldMap.get("alias");

      if (!BeeUtils.isEmpty(alias)) {
        query.append(" AS ").append(alias);
      }
    }
    query.append(" FROM ");

    List<FromSource> fromList = ss.getFrom();

    for (FromSource from : fromList) {
      query.append(from.getJoinMode()).append(
          from.getFrom(this, paramMode));
    }

    Condition whereClause = ss.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      query.append(" WHERE ").append(whereClause.getCondition(this, paramMode));
    }

    List<String> groupList = ss.getGroupBy();

    if (!BeeUtils.isEmpty(groupList)) {
      query.append(" GROUP BY ");
      for (int i = 0; i < groupList.size(); i++) {
        String group = groupList.get(i);
        if (i > 0) {
          query.append(", ");
        }
        query.append(group);
      }
    }

    List<Map<String, Object>> orderList = ss.getOrderBy();

    if (!BeeUtils.isEmpty(orderList)) {
      query.append(" ORDER BY ");
      for (int i = 0; i < orderList.size(); i++) {
        Map<String, Object> order = orderList.get(i);
        if (i > 0) {
          query.append(", ");
        }
        query.append(order.get("field"));
        if ((Boolean) order.get("desc")) {
          query.append(" DESC");
        }
      }
    }

    Condition havingClause = ss.getHaving();

    if (!BeeUtils.isEmpty(havingClause)) {
      query.append(" HAVING ").append(
          havingClause.getCondition(this, paramMode));
    }

    List<SqlSelect> unionList = ss.getUnion();

    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        query.append(ss.getUnionMode()).append(union.getQuery(this, paramMode));
      }
    }
    return parseQuotes(query.toString());
  }

  String getUpdate(SqlUpdate su, boolean paramMode) {
    Assert.notNull(su);
    Assert.state(!su.isEmpty());
    // TODO Auto-generated method stub
    Assert.noNulls(paramMode);
    return null;
  }
}
