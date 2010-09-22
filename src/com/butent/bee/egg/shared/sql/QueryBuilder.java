package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder {

  private List<Map<String, String>> fieldList = new ArrayList<Map<String, String>>();
  private List<FromSource> fromList = new ArrayList<FromSource>();
  private Condition whereClause;
  private List<String> groupList;
  private List<Map<String, Object>> orderList;
  private Condition havingClause;
  private List<QueryBuilder> unionList;

  private boolean queryConditionMode;
  private String unionMode;

  // Constructors -----------------------------------------------------------
  public QueryBuilder() {
    setQueryConditionMode(false);
    setUnionAllMode(true);
  }

  public QueryBuilder(QueryBuilder qb) {
    fieldList = new ArrayList<Map<String, String>>(qb.fieldList);
    Collections.copy(fieldList, qb.fieldList);

    fromList = new ArrayList<FromSource>(qb.fromList);
    Collections.copy(fromList, qb.fromList);

    if (!BeeUtils.isEmpty(qb.whereClause)) {
      whereClause = qb.whereClause;
    }
    if (!BeeUtils.isEmpty(qb.groupList)) {
      groupList = new ArrayList<String>(qb.groupList);
      Collections.copy(groupList, qb.groupList);
    }
    if (!BeeUtils.isEmpty(qb.orderList)) {
      orderList = new ArrayList<Map<String, Object>>(qb.orderList);
      Collections.copy(orderList, qb.orderList);
    }
    if (!BeeUtils.isEmpty(qb.havingClause)) {
      havingClause = qb.havingClause;
    }
    if (!BeeUtils.isEmpty(qb.unionList)) {
      unionList = new ArrayList<QueryBuilder>(qb.unionList);
      Collections.copy(unionList, qb.unionList);
    }
    queryConditionMode = qb.queryConditionMode;
    unionMode = qb.unionMode;
  }

  public QueryBuilder addAvg(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addAggregate("AVG", expr, alias);
    return this;
  }

  public QueryBuilder addAvg(String source, String field, String alias) {
    return addAvg(SqlUtils.fields(source, field), alias);
  }

  public QueryBuilder addConstant(Object constant, String alias) {
    Assert.notNull(constant);
    Assert.notEmpty(alias);

    addField(constant.toString(), alias); // TODO: padaryti protingesni
    return this;
  }

  public QueryBuilder addCount(String alias) {
    return addCount(null, alias);
  }

  public QueryBuilder addCount(String expr, String alias) {
    Assert.notEmpty(alias);

    String xpr;
    if (BeeUtils.isEmpty(expr)) {
      xpr = "*";
    } else {
      xpr = expr.trim();
    }
    addAggregate("COUNT", xpr, alias);
    return this;
  }

  public QueryBuilder addDistinct(String source, String field) {
    Assert.notEmpty(source);
    Assert.notEmpty(field);

    addField("DISTINCT " + SqlUtils.fields(source, field), null);
    return this;
  }

  public QueryBuilder addExpr(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addField(expr, alias);
    return this;
  }

  public QueryBuilder addField(String source, String field, String alias) {
    Assert.notEmpty(source);
    Assert.notEmpty(field);

    addField(SqlUtils.fields(source, field), alias);
    return this;
  }

  public QueryBuilder addFields(String source, String... fields) {
    Assert.notEmpty(source);
    Assert.noNulls((Object[]) fields);

    for (String fld : fields) {
      if (BeeUtils.isEmpty(fld)) {
        continue;
      }
      addField(SqlUtils.fields(source, fld), null);
    }
    return this;
  }

  public QueryBuilder addFrom(QueryBuilder source, String alias) {
    if (BeeUtils.isEmpty(fromList)) {
      addFrom(new FromSingle(source, alias));
    } else {
      addFrom(new FromList(source, alias));
    }
    return this;
  }

  public QueryBuilder addFrom(String source) {
    addFrom(source, null);
    return this;
  }

  public QueryBuilder addFrom(String source, String alias) {
    if (BeeUtils.isEmpty(fromList)) {
      addFrom(new FromSingle(source, alias));
    } else {
      addFrom(new FromList(source, alias));
    }
    return this;
  }

  public QueryBuilder addFromFull(QueryBuilder source, String alias,
      Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromFull(source, alias, on));
    return this;
  }

  public QueryBuilder addFromFull(String source, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFromFull(source, null, on);
    return this;
  }

  public QueryBuilder addFromFull(String source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromFull(source, alias, on));
    return this;
  }

  public QueryBuilder addFromInner(QueryBuilder source, String alias,
      Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromInner(source, alias, on));
    return this;
  }

  public QueryBuilder addFromInner(String source, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFromInner(source, null, on);
    return this;
  }

  public QueryBuilder addFromInner(String source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromInner(source, alias, on));
    return this;
  }

  public QueryBuilder addFromLeft(QueryBuilder source, String alias,
      Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromLeft(source, alias, on));
    return this;
  }

  public QueryBuilder addFromLeft(String source, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFromLeft(source, null, on);
    return this;
  }

  public QueryBuilder addFromLeft(String source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromLeft(source, alias, on));
    return this;
  }

  public QueryBuilder addFromRight(QueryBuilder source, String alias,
      Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromRight(source, alias, on));
    return this;
  }

  public QueryBuilder addFromRight(String source, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFromRight(source, null, on);
    return this;
  }

  public QueryBuilder addFromRight(String source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromRight(source, alias, on));
    return this;
  }

  // Group ------------------------------------------------------------------
  public QueryBuilder addGroup(String... group) {
    Assert.noNulls((Object[]) group);

    if (BeeUtils.isEmpty(groupList)) {
      groupList = new ArrayList<String>();
    }
    for (String grp : group) {
      if (!BeeUtils.isEmpty(grp)) {
        groupList.add(grp);
      }
    }
    return this;
  }

  public QueryBuilder addMax(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addAggregate("MAX", expr, alias);
    return this;
  }

  public QueryBuilder addMax(String source, String field, String alias) {
    return addMax(SqlUtils.fields(source, field), alias);
  }

  public QueryBuilder addMin(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addAggregate("MIN", expr, alias);
    return this;
  }

  public QueryBuilder addMin(String source, String field, String alias) {
    return addMin(SqlUtils.fields(source, field), alias);
  }

  public QueryBuilder addOrder(String... order) {
    Assert.noNulls((Object[]) order);

    for (String ord : order) {
      if (!BeeUtils.isEmpty(ord)) {
        addOrder(ord, false);
      }
    }
    return this;
  }

  public QueryBuilder addOrder(String order, Boolean desc) {
    Assert.notEmpty(order);

    Map<String, Object> fldMap = new HashMap<String, Object>(2);
    fldMap.put("field", order);
    fldMap.put("desc", !BeeUtils.isEmpty(desc));

    if (BeeUtils.isEmpty(orderList)) {
      orderList = new ArrayList<Map<String, Object>>();
    }
    orderList.add(fldMap);

    return this;
  }

  public QueryBuilder addSum(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addAggregate("SUM", expr, alias);
    return this;
  }

  public QueryBuilder addSum(String source, String field, String alias) {
    return addSum(SqlUtils.fields(source, field), alias);
  }

  // Union ------------------------------------------------------------------
  public QueryBuilder addUnion(QueryBuilder... union) {
    Assert.noNulls((Object[]) union);

    if (BeeUtils.isEmpty(unionList)) {
      this.unionList = new ArrayList<QueryBuilder>();
    }
    for (QueryBuilder un : union) {
      if (!un.isEmpty()) {
        this.unionList.add(un);
      }
    }
    return this;
  }

  // Getters ----------------------------------------------------------------
  public List<String> getFieldAliases() {
    List<String> fldList = new ArrayList<String>();

    for (Map<String, String> fldMap : fieldList) {
      String als = fldMap.get("alias");

      if (BeeUtils.isEmpty(als)) {
        als = fldMap.get("field");
      }
      fldList.add(als);
    }
    return fldList;
  }

  public List<String> getFieldNames() {
    List<String> fldList = new ArrayList<String>();

    for (Map<String, String> fldMap : fieldList) {
      fldList.add(fldMap.get("field"));
    }
    return fldList;
  }

  public List<FromSource> getFrom() {
    return fromList;
  }

  public List<String> getGroupBy() {
    return groupList;
  }

  public Condition getHaving() {
    return havingClause;
  }

  public List<Map<String, Object>> getOrderBy() {
    return orderList;
  }

  public Map<Integer, Object> getParameters() {
    return getParameters(queryConditionMode);
  }

  public Map<Integer, Object> getParameters(boolean queryMode) {
    if (!queryMode) {
      return null;
    }
    Map<Integer, Object> params = new HashMap<Integer, Object>();
    Integer paramIndex = 0;

    for (FromSource from : fromList) {
      paramIndex = conditionParameters(from.getQueryParameters(), paramIndex,
          params);
    }
    if (!BeeUtils.isEmpty(whereClause)) {
      paramIndex = conditionParameters(whereClause.getQueryParameters(),
          paramIndex, params);
    }
    if (!BeeUtils.isEmpty(havingClause)) {
      paramIndex = conditionParameters(havingClause.getQueryParameters(),
          paramIndex, params);
    }
    if (!BeeUtils.isEmpty(unionList)) {
      for (QueryBuilder union : unionList) {
        Map<Integer, Object> paramMap = union.getParameters(true);

        if (!BeeUtils.isEmpty(paramMap)) {
          for (int i = 0; i < paramMap.size(); i++) {
            params.put(++paramIndex, paramMap.get(i + 1));
          }
        }
      }
    }
    return params;
  }

  public String getQuery() {
    return getQuery(queryConditionMode);
  }

  public String getQuery(boolean queryMode) {
    Assert.state(!isEmpty(), "Empty instance");

    StringBuilder query = new StringBuilder("SELECT ");

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

    for (FromSource from : fromList) {
      query.append(from.getJoinMode()).append(from.getCondition(queryMode));
    }

    if (!BeeUtils.isEmpty(whereClause)) {
      query.append(" WHERE ").append(whereClause.getCondition(queryMode));
    }

    if (!BeeUtils.isEmpty(this.groupList)) {
      query.append(" GROUP BY ");
      for (int i = 0; i < groupList.size(); i++) {
        String group = groupList.get(i);
        if (i > 0) {
          query.append(", ");
        }
        query.append(group);
      }
    }

    if (!BeeUtils.isEmpty(this.orderList)) {
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

    if (!BeeUtils.isEmpty(havingClause)) {
      query.append(" HAVING ").append(havingClause.getCondition(queryMode));
    }

    if (!BeeUtils.isEmpty(this.unionList)) {
      for (QueryBuilder union : unionList) {
        query.append(unionMode).append(union.getQuery(queryMode));
      }
    }
    return query.toString();
  }

  public List<String> getSources(String source) {
    Assert.notEmpty(source);

    List<String> lst = new ArrayList<String>();

    for (FromSource from : this.fromList) {
      Object src = from.getSource();

      if (src instanceof String && source.equals(src)) {
        String als = from.getAlias();

        if (BeeUtils.isEmpty(als)) {
          als = source;
        }
        lst.add(als);
      }
    }
    return lst;
  }

  public List<QueryBuilder> getUnion() {
    return unionList;
  }

  public Condition getWhere() {
    return whereClause;
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(fieldList) || BeeUtils.isEmpty(fromList);
  }

  // Fields -----------------------------------------------------------------
  public QueryBuilder resetFields() {
    this.fieldList.clear();
    return this;
  }

  // Order ------------------------------------------------------------------
  public QueryBuilder resetOrder() {
    if (!BeeUtils.isEmpty(orderList)) {
      orderList.clear();
    }
    return this;
  }

  // Having -----------------------------------------------------------------
  public QueryBuilder setHaving(Condition having) {
    Assert.notEmpty(having);

    havingClause = having;
    return this;
  }

  public void setQueryConditionMode(boolean mode) {
    queryConditionMode = mode;
  }

  public void setUnionAllMode(boolean allMode) {
    if (allMode) {
      unionMode = " UNION ALL ";
    } else {
      unionMode = " UNION ";
    }
  }

  // Where ------------------------------------------------------------------
  public QueryBuilder setWhere(Condition clause) {
    whereClause = clause;
    return this;
  }

  // Aggregates -------------------------------------------------------------
  private void addAggregate(String fnc, String expr, String alias) {
    addField(fnc + "(" + expr + ")", alias);
  }

  private void addField(String expr, String alias) {
    Map<String, String> fldMap = new HashMap<String, String>(2);
    fldMap.put("field", expr);

    if (!BeeUtils.isEmpty(alias)) {
      fldMap.put("alias", SqlUtils.sqlQuote(alias));
    }
    fieldList.add(fldMap);
  }

  // From -------------------------------------------------------------------
  private void addFrom(FromSource from) {
    fromList.add(from);
  }

  private Integer conditionParameters(List<Object> paramList, Integer index,
      Map<Integer, Object> params) {
    Integer idx = index;

    if (!BeeUtils.isEmpty(paramList)) {
      for (Object prm : paramList) {
        params.put(++idx, prm);
      }
    }
    return idx;
  }
}
