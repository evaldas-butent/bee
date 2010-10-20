package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class SqlBuilder {

  String getQuery(QueryBuilder qb, boolean queryMode) {
    Assert.notEmpty(qb);
    Assert.state(!qb.isEmpty(), "Empty instance");

    StringBuilder query = new StringBuilder("SELECT ");

    List<Map<String, String>> fieldList = qb.getFields();

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

    List<FromSource> fromList = qb.getFrom();

    for (FromSource from : fromList) {
      query.append(from.getJoinMode()).append(
          from.getCondition(this, queryMode));
    }

    Condition whereClause = qb.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      query.append(" WHERE ").append(whereClause.getCondition(this, queryMode));
    }

    List<String> groupList = qb.getGroupBy();

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

    List<Map<String, Object>> orderList = qb.getOrderBy();

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

    Condition havingClause = qb.getHaving();

    if (!BeeUtils.isEmpty(havingClause)) {
      query.append(" HAVING ").append(
          havingClause.getCondition(this, queryMode));
    }

    List<QueryBuilder> unionList = qb.getUnion();

    if (!BeeUtils.isEmpty(unionList)) {
      for (QueryBuilder union : unionList) {
        query.append(qb.getUnionMode()).append(union.getQuery(this, queryMode));
      }
    }
    return parseQuotes(query.toString());
  }

  protected String parseQuotes(String query) {
    return query.replaceAll(SqlUtils.SQL_OPEN_QUOTE + "|"
        + SqlUtils.SQL_CLOSE_QUOTE, "");
  }
}
