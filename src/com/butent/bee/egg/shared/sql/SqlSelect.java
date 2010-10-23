package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlSelect extends SqlQuery {

  private List<Map<String, String>> fieldList = new ArrayList<Map<String, String>>();
  private List<FromSource> fromList = new ArrayList<FromSource>();
  private Condition whereClause;
  private List<String> groupList;
  private List<Map<String, Object>> orderList;
  private Condition havingClause;
  private List<SqlSelect> unionList;

  private String unionMode;

  // Constructors -----------------------------------------------------------
  public SqlSelect() {
    setParamMode(false);
    setUnionAllMode(true);
  }

  public SqlSelect(SqlSelect ss) {
    fieldList = new ArrayList<Map<String, String>>(ss.fieldList);
    Collections.copy(fieldList, ss.fieldList);

    fromList = new ArrayList<FromSource>(ss.fromList);
    Collections.copy(fromList, ss.fromList);

    if (!BeeUtils.isEmpty(ss.whereClause)) {
      whereClause = ss.whereClause;
    }
    if (!BeeUtils.isEmpty(ss.groupList)) {
      groupList = new ArrayList<String>(ss.groupList);
      Collections.copy(groupList, ss.groupList);
    }
    if (!BeeUtils.isEmpty(ss.orderList)) {
      orderList = new ArrayList<Map<String, Object>>(ss.orderList);
      Collections.copy(orderList, ss.orderList);
    }
    if (!BeeUtils.isEmpty(ss.havingClause)) {
      havingClause = ss.havingClause;
    }
    if (!BeeUtils.isEmpty(ss.unionList)) {
      unionList = new ArrayList<SqlSelect>(ss.unionList);
      Collections.copy(unionList, ss.unionList);
    }
    setParamMode(ss.getParamMode());
    unionMode = ss.unionMode;
  }

  public SqlSelect addAvg(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addAggregate("AVG", expr, alias);
    return this;
  }

  public SqlSelect addAvg(String source, String field, String alias) {
    return addAvg(SqlUtils.fields(source, field), alias);
  }

  public SqlSelect addConstant(Object constant, String alias) {
    Assert.notNull(constant);
    Assert.notEmpty(alias);

    addField(constant.toString(), alias); // TODO: padaryti protingesni
    return this;
  }

  public SqlSelect addCount(String alias) {
    return addCount(null, alias);
  }

  public SqlSelect addCount(String expr, String alias) {
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

  public SqlSelect addDistinct(String source, String field) {
    Assert.notEmpty(source);
    Assert.notEmpty(field);

    addField("DISTINCT " + SqlUtils.fields(source, field), null);
    return this;
  }

  public SqlSelect addExpr(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addField(expr, alias);
    return this;
  }

  public SqlSelect addField(String source, String field, String alias) {
    Assert.notEmpty(source);
    Assert.notEmpty(field);

    addField(SqlUtils.fields(source, field), alias);
    return this;
  }

  public SqlSelect addFields(String source, String... fields) {
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

  public SqlSelect addFrom(SqlSelect source, String alias) {
    if (BeeUtils.isEmpty(fromList)) {
      addFrom(new FromSingle(source, alias));
    } else {
      addFrom(new FromList(source, alias));
    }
    return this;
  }

  public SqlSelect addFrom(String source) {
    addFrom(source, null);
    return this;
  }

  public SqlSelect addFrom(String source, String alias) {
    if (BeeUtils.isEmpty(fromList)) {
      addFrom(new FromSingle(source, alias));
    } else {
      addFrom(new FromList(source, alias));
    }
    return this;
  }

  public SqlSelect addFromFull(SqlSelect source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromFull(source, alias, on));
    return this;
  }

  public SqlSelect addFromFull(String source, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFromFull(source, null, on);
    return this;
  }

  public SqlSelect addFromFull(String source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromFull(source, alias, on));
    return this;
  }

  public SqlSelect addFromInner(SqlSelect source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromInner(source, alias, on));
    return this;
  }

  public SqlSelect addFromInner(String source, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFromInner(source, null, on);
    return this;
  }

  public SqlSelect addFromInner(String source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromInner(source, alias, on));
    return this;
  }

  public SqlSelect addFromLeft(SqlSelect source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromLeft(source, alias, on));
    return this;
  }

  public SqlSelect addFromLeft(String source, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFromLeft(source, null, on);
    return this;
  }

  public SqlSelect addFromLeft(String source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromLeft(source, alias, on));
    return this;
  }

  public SqlSelect addFromRight(SqlSelect source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromRight(source, alias, on));
    return this;
  }

  public SqlSelect addFromRight(String source, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFromRight(source, null, on);
    return this;
  }

  public SqlSelect addFromRight(String source, String alias, Condition on) {
    Assert.notEmpty(fromList, "Wrong first FROM source");

    addFrom(new FromRight(source, alias, on));
    return this;
  }

  // Group ------------------------------------------------------------------
  public SqlSelect addGroup(String... group) {
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

  public SqlSelect addMax(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addAggregate("MAX", expr, alias);
    return this;
  }

  public SqlSelect addMax(String source, String field, String alias) {
    return addMax(SqlUtils.fields(source, field), alias);
  }

  public SqlSelect addMin(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addAggregate("MIN", expr, alias);
    return this;
  }

  public SqlSelect addMin(String source, String field, String alias) {
    return addMin(SqlUtils.fields(source, field), alias);
  }

  public SqlSelect addOrder(String... order) {
    Assert.noNulls((Object[]) order);

    for (String ord : order) {
      if (!BeeUtils.isEmpty(ord)) {
        addOrder(ord, false);
      }
    }
    return this;
  }

  public SqlSelect addOrderDesc(String... order) {
    Assert.noNulls((Object[]) order);

    for (String ord : order) {
      if (!BeeUtils.isEmpty(ord)) {
        addOrder(ord, true);
      }
    }
    return this;
  }

  public SqlSelect addSum(String expr, String alias) {
    Assert.notEmpty(expr);
    Assert.notEmpty(alias);

    addAggregate("SUM", expr, alias);
    return this;
  }

  public SqlSelect addSum(String source, String field, String alias) {
    return addSum(SqlUtils.fields(source, field), alias);
  }

  // Union ------------------------------------------------------------------
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

  public List<Map<String, String>> getFields() {
    return fieldList;
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

  @Override
  public Map<Integer, Object> getParameters() {
    Map<Integer, Object> params = new HashMap<Integer, Object>();
    Integer paramIndex = 0;

    for (FromSource from : fromList) {
      paramIndex = conditionParameters(from.getParameters(), paramIndex, params);
    }
    if (!BeeUtils.isEmpty(whereClause)) {
      paramIndex = conditionParameters(whereClause.getParameters(), paramIndex,
          params);
    }
    if (!BeeUtils.isEmpty(havingClause)) {
      paramIndex = conditionParameters(havingClause.getParameters(),
          paramIndex, params);
    }
    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        Map<Integer, Object> paramMap = union.getParameters();

        if (!BeeUtils.isEmpty(paramMap)) {
          for (int i = 0; i < paramMap.size(); i++) {
            params.put(++paramIndex, paramMap.get(i + 1));
          }
        }
      }
    }
    return params;
  }

  @Override
  public String getQuery(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);

    return builder.getQuery(this, paramMode);
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

  public List<SqlSelect> getUnion() {
    return unionList;
  }

  public String getUnionMode() {
    return unionMode;
  }

  public Condition getWhere() {
    return whereClause;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(fieldList) || BeeUtils.isEmpty(fromList);
  }

  // Fields -----------------------------------------------------------------
  public SqlSelect resetFields() {
    this.fieldList.clear();
    return this;
  }

  // Order ------------------------------------------------------------------
  public SqlSelect resetOrder() {
    if (!BeeUtils.isEmpty(orderList)) {
      orderList.clear();
    }
    return this;
  }

  // Having -----------------------------------------------------------------
  public SqlSelect setHaving(Condition having) {
    Assert.notEmpty(having);

    havingClause = having;
    return this;
  }

  public void setUnionAllMode(boolean allMode) {
    unionMode = allMode ? " UNION ALL " : " UNION ";
  }

  // Where ------------------------------------------------------------------
  public SqlSelect setWhere(Condition clause) {
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

  private SqlSelect addOrder(String order, Boolean desc) {
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
