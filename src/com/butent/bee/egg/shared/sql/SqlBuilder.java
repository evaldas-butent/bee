package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.SqlCreate.SqlField;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public abstract class SqlBuilder {

  public String getTables() {
    return "SELECT table_name FROM information_schema.tables";
  }

  public String sqlTransform(Object x) {
    String s = BeeUtils.transform(x);

    if (x instanceof CharSequence) {
      s = "'" + s.replaceAll("'", "''") + "'";
    }
    return s;
  }

  protected String sqlFieldType(SqlField field) {
    StringBuilder xpr = new StringBuilder();

    xpr.append(sqlType(field.getType(), field.getPrecission(), field.getScale()));

    Keywords[] prm = field.getParams();

    if (!BeeUtils.isEmpty(prm)) {
      for (Keywords p : prm) {
        xpr.append(" ").append(sqlKeyword(p));
      }
    }
    return xpr.toString();
  }

  protected Object sqlKeyword(Keywords prm) {
    switch (prm) {
      case NOTNULL:
        return "NOT NULL";
      case PRIMARY:
        return "PRIMARY KEY";
      case UNIQUE:
        return "UNIQUE";
      default:
        Assert.unsupported("Unsupported keyword: " + prm.name());
        return null;
    }
  }

  protected abstract String sqlQuote(String value);

  protected Object sqlType(DataTypes type, int precission, int scale) {
    switch (type) {
      case BOOLEAN:
        return "BIT";
      case INTEGER:
        return "INTEGER";
      case LONG:
        return "BIGINT";
      case DOUBLE:
        return "DOUBLE";
      case NUMERIC:
        return "NUMERIC(" + precission + ", " + scale + ")";
      case CHAR:
        return "CHAR(" + precission + ")";
      case STRING:
        return "VARCHAR(" + precission + ")";
      default:
        Assert.unsupported("Unsupported data type: " + type.name());
        return null;
    }
  }

  String getCreate(SqlCreate sc, boolean paramMode) {
    Assert.notNull(sc);
    Assert.state(!sc.isEmpty());

    StringBuilder query = new StringBuilder("CREATE TABLE ");

    query.append(sc.getTarget().getSqlString(this, paramMode));

    List<SqlField> fieldList = sc.getFields();

    if (!BeeUtils.isEmpty(sc.getSource())) {
      query.append(" AS ");
      query.append(sc.getSource().getSqlString(this, paramMode));
    } else {
      query.append(" (");

      for (int i = 0; i < fieldList.size(); i++) {
        if (i > 0) {
          query.append(", ");
        }
        SqlField field = fieldList.get(i);
        query.append(field.getName().getSqlString(this, paramMode))
          .append(" ").append(sqlFieldType(field));
      }
      query.append(")");
    }
    return query.toString();
  }

  String getDelete(SqlDelete sd, boolean paramMode) {
    Assert.notNull(sd);
    Assert.state(!sd.isEmpty());

    StringBuilder query = new StringBuilder("DELETE ");

    query.append(" FROM ").append(sd.getTarget().getSqlString(this, paramMode));

    List<IsFrom> fromList = sd.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getJoinMode()).append(
            from.getSqlString(this, paramMode));
      }
    }
    query.append(" WHERE ").append(sd.getWhere().getSqlString(this, paramMode));

    return query.toString();
  }

  String getDrop(SqlDrop sd, boolean paramMode) {
    Assert.notNull(sd);
    Assert.state(!sd.isEmpty());

    StringBuilder query = new StringBuilder("DROP TABLE ");

    query.append(sd.getTarget().getSqlString(this, paramMode));

    return query.toString();
  }

  String getIndex(SqlIndex si, boolean paramMode) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());

    StringBuilder query = new StringBuilder("CREATE");

    if (si.isUnique()) {
      query.append(" UNIQUE");
    }
    query.append(" INDEX ");
    query.append(si.getName().getSqlString(this, paramMode));
    query.append(" ON ").append(si.getTarget().getSqlString(this, paramMode));
    query.append(" (");

    IsExpression[] cols = si.getColumns();

    for (int i = 0; i < cols.length; i++) {
      if (i > 0) {
        query.append(", ");
      }
      query.append(cols[i].getSqlString(this, paramMode));
    }
    query.append(")");

    return query.toString();
  }

  String getInsert(SqlInsert si, boolean paramMode) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());

    StringBuilder query = new StringBuilder("INSERT INTO ");

    query.append(si.getTarget().getSqlString(this, paramMode));

    List<IsExpression> fieldList = si.getFields();

    query.append(" (");

    for (int i = 0; i < fieldList.size(); i++) {
      if (i > 0) {
        query.append(", ");
      }
      IsExpression field = fieldList.get(i);
      query.append(field.getSqlString(this, paramMode));
    }
    query.append(") ");

    if (!BeeUtils.isEmpty(si.getSource())) {
      query.append(si.getSource().getSqlString(this, paramMode));
    } else {
      List<IsExpression> valueList = si.getValues();

      if (!BeeUtils.isEmpty(valueList)) {
        query.append("VALUES (");

        for (int i = 0; i < valueList.size(); i++) {
          if (i > 0) {
            query.append(", ");
          }
          IsExpression value = valueList.get(i);
          query.append(value.getSqlString(this, paramMode));
        }
        query.append(")");
      }
    }
    return query.toString();
  }

  String getQuery(SqlSelect ss, boolean paramMode) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    StringBuilder query = new StringBuilder("SELECT ");

    List<Object[]> fieldList = ss.getFields();

    for (int i = 0; i < fieldList.size(); i++) {
      if (i > 0) {
        query.append(", ");
      }
      Object[] fldEntry = fieldList.get(i);
      IsExpression field = (IsExpression) fldEntry[SqlSelect.FIELD_EXPR];
      query.append(field.getSqlString(this, paramMode));

      String alias = (String) fldEntry[SqlSelect.FIELD_ALIAS];

      if (!BeeUtils.isEmpty(alias)) {
        query.append(" AS ").append(sqlQuote(alias));
      }
    }
    List<IsFrom> fromList = ss.getFrom();

    query.append(" FROM ");

    for (IsFrom from : fromList) {
      query.append(from.getJoinMode())
        .append(from.getSqlString(this, paramMode));
    }
    IsCondition whereClause = ss.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      query.append(" WHERE ").append(whereClause.getSqlString(this, paramMode));
    }
    List<IsExpression> groupList = ss.getGroupBy();

    if (!BeeUtils.isEmpty(groupList)) {
      query.append(" GROUP BY ");

      for (int i = 0; i < groupList.size(); i++) {
        if (i > 0) {
          query.append(", ");
        }
        String group = groupList.get(i).getSqlString(this, paramMode);
        query.append(group);
      }
    }
    List<Object[]> orderList = ss.getOrderBy();

    if (!BeeUtils.isEmpty(orderList)) {
      query.append(" ORDER BY ");

      for (int i = 0; i < orderList.size(); i++) {
        if (i > 0) {
          query.append(", ");
        }
        Object[] orderEntry = orderList.get(i);
        IsExpression order = (IsExpression) orderEntry[SqlSelect.ORDER_EXPR];
        query.append(order.getSqlString(this, paramMode));

        if ((Boolean) orderEntry[SqlSelect.ORDER_DESC]) {
          query.append(" DESC");
        }
      }
    }
    IsCondition havingClause = ss.getHaving();

    if (!BeeUtils.isEmpty(havingClause)) {
      query.append(" HAVING ")
        .append(havingClause.getSqlString(this, paramMode));
    }
    List<SqlSelect> unionList = ss.getUnion();

    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        query.append(ss.getUnionMode())
          .append(union.getSqlString(this, paramMode));
      }
    }
    return query.toString();
  }

  String getUpdate(SqlUpdate su, boolean paramMode) {
    Assert.notNull(su);
    Assert.state(!su.isEmpty());

    StringBuilder query = new StringBuilder("UPDATE ");

    query.append(su.getTarget().getSqlString(this, paramMode));

    List<IsExpression[]> updates = su.getUpdates();

    query.append(" SET ");

    for (int i = 0; i < updates.size(); i++) {
      if (i > 0) {
        query.append(", ");
      }
      IsExpression[] updateEntry = updates.get(i);
      IsExpression field = updateEntry[SqlUpdate.FIELD_INDEX];
      query.append(field.getSqlString(this, paramMode));

      IsExpression value = updateEntry[SqlUpdate.VALUE_INDEX];
      query.append(" = ").append(value.getSqlString(this, paramMode));
    }
    List<IsFrom> fromList = su.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getJoinMode())
          .append(from.getSqlString(this, paramMode));
      }
    }
    IsCondition whereClause = su.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      query.append(" WHERE ").append(whereClause.getSqlString(this, paramMode));
    }
    return query.toString();
  }
}
