package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.SqlCreate.SqlField;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class SqlBuilder {

  protected String sqlKeyword(Keywords option, Object... params) {
    switch (option) {
      case NOT_NULL:
        return "NOT NULL";

      case CREATE_INDEX:
        StringBuilder index = new StringBuilder("CREATE");

        if (!BeeUtils.isEmpty(params[0])) {
          index.append(" UNIQUE");
        }
        index.append(" INDEX ")
          .append(params[2])
          .append(" ON ")
          .append(params[1])
          .append(" (").append(BeeUtils.join(params, ", ", 3)).append(")");
        return index.toString();

      case ADD_CONSTRAINT:
        StringBuilder cmd = new StringBuilder("ALTER TABLE ")
          .append(params[0])
          .append(" ADD CONSTRAINT ")
          .append(params[1]);

        Object[] prm = new Object[params.length - 3];
        for (int i = 3; i < params.length; i++) {
          prm[i - 3] = params[i];
        }
        cmd.append(sqlKeyword((Keywords) params[2], prm));
        return cmd.toString();

      case PRIMARYKEY:
        StringBuilder primary = new StringBuilder(" PRIMARY KEY (")
          .append(BeeUtils.join(params, ", ")).append(")");
        return primary.toString();

      case FOREIGNKEY:
        StringBuilder foreign = new StringBuilder(" FOREIGN KEY (")
          .append(params[0])
          .append(") REFERENCES ")
          .append(params[1])
          .append(" (").append(params[2]).append(")");

        if (!BeeUtils.isEmpty(params[3])) {
          foreign.append(" ON DELETE ")
            .append(sqlKeyword((Keywords) params[3]));
        }
        return foreign.toString();

      case CASCADE:
        return "CASCADE";

      case SET_NULL:
        return "SET NULL";

      case DB_TABLES:
        IsCondition tableWh = null;

        if (!BeeUtils.isEmpty(params[0])) {
          tableWh = SqlUtils.and(SqlUtils.equal("t", "table_name", params[0]),
              SqlUtils.equal("t", "table_type", "BASE TABLE"));
        }
        return new SqlSelect()
          .addFields("t", "table_name")
          .addFrom("information_schema.tables", "t")
          .setWhere(tableWh)
          .getQuery(this);

      case DB_FOREIGNKEYS:
        IsCondition foreignWh = SqlUtils.equal("c", "constraint_type", "FOREIGN KEY");

        if (!BeeUtils.isEmpty(params[0])) {
          foreignWh = SqlUtils.and(foreignWh, SqlUtils.equal("c", "table_name", params[0]));
        }
        return new SqlSelect()
          .addFields("c", "constraint_name")
          .addFrom("information_schema.table_constraints", "c")
          .setWhere(foreignWh)
          .getQuery(this);

      case DROP_TABLE:
        return "DROP TABLE " + params[0];

      case DROP_FOREIGNKEY:
        StringBuilder drop = new StringBuilder("ALTER TABLE ")
          .append(params[0])
          .append(" DROP CONSTRAINT ")
          .append(params[1]);
        return drop.toString();

      case TEMPORARY:
        return " TEMPORARY";

      case TEMPORARY_NAME:
        return (String) params[0];

      default:
        Assert.unsupported("Unsupported keyword: " + option);
        return null;
    }
  }

  protected abstract String sqlQuote(String value);

  protected String sqlTransform(Object x) {
    String s = BeeUtils.transformNoTrim(x);

    if (x instanceof CharSequence) {
      s = "'" + s.replaceAll("'", "''") + "'";
    }
    return s;
  }

  protected Object sqlType(DataTypes type, int precision, int scale) {
    switch (type) {
      case BOOLEAN:
        return "BIT";
      case INTEGER:
        return "INTEGER";
      case LONG:
        return "BIGINT";
      case FLOAT:
        return "FLOAT";
      case DOUBLE:
        return "DOUBLE";
      case NUMERIC:
        return "NUMERIC(" + precision + ", " + scale + ")";
      case CHAR:
        return "CHAR(" + precision + ")";
      case STRING:
        return "VARCHAR(" + precision + ")";
      default:
        Assert.unsupported("Unsupported data type: " + type.name());
        return null;
    }
  }

  String getCommand(SqlCommand sc, boolean paramMode) {
    Assert.notNull(sc);
    Assert.state(!sc.isEmpty());

    List<Object> params = new ArrayList<Object>();

    for (Object prm : sc.getParameters()) {
      if (prm instanceof IsSql) {
        params.add(((IsSql) prm).getSqlString(this, paramMode));
      } else {
        params.add(prm);
      }
    }
    return sqlKeyword(sc.getCommand(), params.toArray());
  }

  String getCreate(SqlCreate sc, boolean paramMode) {
    Assert.notNull(sc);
    Assert.state(!sc.isEmpty());

    StringBuilder query = new StringBuilder("CREATE");

    if (sc.isTemporary()) {
      query.append(sqlKeyword(Keywords.TEMPORARY));
    }
    query.append(" TABLE ");

    query.append(sc.getTarget().getSqlString(this, paramMode));

    List<SqlField> fieldList = sc.getFields();

    if (!BeeUtils.isEmpty(sc.getSource())) {
      query.append(" AS ").append(sc.getSource().getSqlString(this, paramMode));
    } else {
      query.append(" (");

      for (int i = 0; i < fieldList.size(); i++) {
        if (i > 0) {
          query.append(", ");
        }
        SqlField field = fieldList.get(i);
        query.append(field.getName().getSqlString(this, paramMode))
          .append(" ").append(sqlType(field.getType(), field.getPrecision(), field.getScale()));

        for (Keywords opt : field.getOptions()) {
          query.append(" ").append(sqlKeyword(opt));
        }
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

    List<IsExpression[]> fieldList = ss.getFields();

    for (int i = 0; i < fieldList.size(); i++) {
      if (i > 0) {
        query.append(", ");
      }
      IsExpression[] fldEntry = fieldList.get(i);
      IsExpression field = fldEntry[SqlSelect.FIELD_EXPR];
      query.append(field.getSqlString(this, paramMode));

      IsExpression alias = fldEntry[SqlSelect.FIELD_ALIAS];

      if (!BeeUtils.isEmpty(alias)) {
        query.append(" AS ").append(alias.getSqlString(this, paramMode));
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
      query.append("=").append(value.getSqlString(this, paramMode));
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
