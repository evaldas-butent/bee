package com.butent.bee.server.sql;

import com.google.common.collect.Maps;

import com.butent.bee.server.sql.BeeConstants.DataType;
import com.butent.bee.server.sql.BeeConstants.Keyword;
import com.butent.bee.server.sql.SqlCreate.SqlField;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Is an abstract class for all SQL server specific SQL builders, contains core 
 * requirements for SQL statements.
 */

public abstract class SqlBuilder {

  protected String sqlKeyword(Keyword option, Map<String, Object> params) {
    switch (option) {
      case NOT_NULL:
        return "NOT NULL";

      case CREATE_INDEX:
        return BeeUtils.concat(1,
            "CREATE", (Boolean) params.get("unique") ? "UNIQUE" : "",
            "INDEX", params.get("name"),
            "ON", params.get("table"),
            BeeUtils.parenthesize(params.get("fields")));

      case ADD_CONSTRAINT:
        return BeeUtils.concat(1,
            "ALTER TABLE", params.get("table"),
            "ADD CONSTRAINT", params.get("name"),
            sqlKeyword((Keyword) params.get("type"), params));

      case PRIMARYKEY:
        return BeeUtils.concat(1,
            "PRIMARY KEY", BeeUtils.parenthesize(params.get("fields")));

      case FOREIGNKEY:
        String foreign = BeeUtils.concat(1,
            "FOREIGN KEY", BeeUtils.parenthesize(params.get("field")),
            "REFERENCES", params.get("refTable"), BeeUtils.parenthesize(params.get("refField")));

        Keyword action = (Keyword) params.get("action");
        if (!BeeUtils.isEmpty(action)) {
          foreign = BeeUtils.concat(1,
              foreign, "ON DELETE", sqlKeyword(action, null));
        }
        return foreign;

      case CASCADE:
        return "CASCADE";

      case SET_NULL:
        return "SET NULL";

      case DB_NAME:
        return "";

      case DB_SCHEMA:
        return "";

      case DB_TABLES:
        IsCondition wh = null;

        Object prm = params.get("dbName");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.equal("t", "table_catalog", prm);
        }
        prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("t", "table_schema", prm));
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("t", "table_name", prm));
        }
        return new SqlSelect()
            .addFields("t", "table_name")
            .addFrom("information_schema.tables", "t")
            .setWhere(wh)
            .getQuery(this);

      case DB_FOREIGNKEYS:
        wh = null;

        prm = params.get("dbName");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh,
              SqlUtils.equal("c", "constraint_catalog", prm),
              SqlUtils.equal("t", "table_catalog", prm));
        }
        prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh,
              SqlUtils.equal("c", "constraint_schema", prm),
              SqlUtils.equal("t", "table_schema", prm));
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("t", "table_name", prm));
        }
        prm = params.get("refTable");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("r", "table_name", prm));
        }
        return new SqlSelect()
            .addField("c", "constraint_name", BeeConstants.FK_NAME)
            .addField("t", "table_name", BeeConstants.FK_TABLE)
            .addField("r", "table_name", BeeConstants.FK_REF_TABLE)
            .addFrom("information_schema.referential_constraints", "c")
            .addFromInner("information_schema.table_constraints", "t",
                SqlUtils.joinUsing("c", "t", "constraint_name"))
            .addFromInner("information_schema.table_constraints", "r",
                SqlUtils.join("c", "unique_constraint_name", "r", "constraint_name"))
            .setWhere(wh)
            .getQuery(this);

      case DROP_TABLE:
        return "DROP TABLE " + params.get("table");

      case DROP_FOREIGNKEY:
        return BeeUtils.concat(1,
            "ALTER TABLE", params.get("table"),
            "DROP CONSTRAINT", params.get("name"));

      case TEMPORARY:
        return "TEMPORARY ";

      case TEMPORARY_NAME:
        return (String) params.get("name");

      case BITAND:
        return "(" + params.get("expression") + "&" + params.get("value") + ")";

      case IF:
        return BeeUtils.concat(1,
            "CASE WHEN", params.get("condition"),
            "THEN", params.get("ifTrue"),
            "ELSE", params.get("ifFalse"),
            "END");

      case CASE:
        StringBuilder xpr = new StringBuilder("CASE ")
            .append(params.get("expression"));

        int cnt = (params.size() - 2) / 2;

        for (int i = 0; i < cnt; i++) {
          xpr.append(" WHEN ")
              .append(params.get("case" + i))
              .append(" THEN ")
              .append(params.get("value" + i));
        }
        xpr.append(" ELSE ")
            .append(params.get("caseElse"))
            .append(" END");

        return xpr.toString();

      case CAST:
        return BeeUtils.concat(1,
            "CAST(" + params.get("expression"),
            "AS",
            sqlType((DataType) params.get("type")
                , (Integer) params.get("precision")
                , (Integer) params.get("scale")) + ")");

      default:
        Assert.unsupported("Unsupported keyword: " + option);
        return null;
    }
  }

  protected abstract String sqlQuote(String value);

  protected String sqlTransform(Object x) {
    String s = null;

    if (x == null) {
      s = "null";

    } else {
      Object val;

      if (x instanceof Value) {
        val = ((Value) x).getObjectValue();
      } else {
        val = x;
      }
      if (val instanceof Boolean) {
        s = (Boolean) val ? "1" : "0";

      } else if (val instanceof JustDate) {
        s = BeeUtils.transform(((JustDate) val).getDay());

      } else if (val instanceof Date) {
        s = BeeUtils.transform(((Date) val).getTime());

      } else if (val instanceof DateTime) {
        s = BeeUtils.transform(((DateTime) val).getTime());

      } else if (val instanceof Number) {
        s = BeeUtils.removeTrailingZeros(BeeUtils.transformNoTrim(val));

      } else {
        s = BeeUtils.transformNoTrim(val);

        if (val instanceof CharSequence) {
          s = "'" + s.replace("'", "''") + "'";
        }
      }
    }
    return s;
  }

  protected String sqlType(DataType type, int precision, int scale) {
    switch (type) {
      case BOOLEAN:
        return "BIT";
      case INTEGER:
      case DATE:
        return "INTEGER";
      case LONG:
      case DATETIME:
        return "BIGINT";
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

  /**
   * Forms a String from an SqlCommand {@code sc} using the specified {@code
   * paramMode}.
   * 
   * @param sc the SqlCommand to use for forming
   * @param paramMode defines if parameter mode is true or false.
   * @return a formed String from the SqlCommand
   */
  String getCommand(SqlCommand sc, boolean paramMode) {
    Assert.notNull(sc);
    Assert.state(!sc.isEmpty());

    Map<String, Object> params = Maps.newHashMap();
    Map<String, Object> paramMap = sc.getParameters();

    if (!BeeUtils.isEmpty(paramMap)) {
      for (String prm : paramMap.keySet()) {
        Object value = paramMap.get(prm);

        if (value instanceof IsSql) {
          value = ((IsSql) value).getSqlString(this, paramMode);
        }
        params.put(prm, value);
      }
    }
    return sqlKeyword(sc.getCommand(), params);
  }

  /**
   * Generates an SQL CREATE query from the specified argument {@code sc}.
   * There are two ways to generate the query. First: by defining a {@code 
   * dataSource}. Second: describing the fields manually. Only one at an 
   * instance of the SqlCreate object is possible. 
   * 
   * @param sc the SqlCreate object
   * @param paramMode sets the parameter mode
   * @return a generated SQL CREATE query
   */
  String getCreate(SqlCreate sc, boolean paramMode) {
    Assert.notNull(sc);
    Assert.state(!sc.isEmpty());

    StringBuilder query = new StringBuilder("CREATE ");

    if (sc.isTemporary()) {
      query.append(sqlKeyword(Keyword.TEMPORARY, null));
    }
    query.append("TABLE ");

    query.append(sc.getTarget().getSqlString(this, paramMode));

    List<SqlField> fieldList = sc.getFields();

    if (!BeeUtils.isEmpty(sc.getDataSource())) {
      query.append(" AS ").append(sc.getDataSource().getSqlString(this, paramMode));
    } else {
      query.append(" (");

      for (int i = 0; i < fieldList.size(); i++) {
        if (i > 0) {
          query.append(", ");
        }
        SqlField field = fieldList.get(i);
        query.append(field.getName().getSqlString(this, paramMode))
            .append(" ").append(sqlType(field.getType(), field.getPrecision(), field.getScale()));

        for (Keyword opt : field.getOptions()) {
          query.append(" ").append(sqlKeyword(opt, null));
        }
      }
      query.append(")");
    }
    return query.toString();
  }

  /**
   * Generates an SQL DELETE query from the specified argument {@code sd}.
   * {@code sd} must have From and Where contitions set.
   * 
   * @param sd the SqlDelete object to use for generating
   * @param paramMode sets the parameter mode
   * @return a generated SQL DELETE query
   */
  String getDelete(SqlDelete sd, boolean paramMode) {
    Assert.notNull(sd);
    Assert.state(!sd.isEmpty());

    StringBuilder query = new StringBuilder("DELETE FROM ");

    query.append(sd.getTarget().getSqlString(this, paramMode));

    List<IsFrom> fromList = sd.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getSqlString(this, paramMode));
      }
    }
    String wh = sd.getWhere().getSqlString(this, paramMode);

    if (!BeeUtils.isEmpty(wh)) {
      query.append(" WHERE ").append(wh);
    }
    return query.toString();
  }

  /**
   * Generates an SQL INSERT query from the specified argument {@code si}.
   * There are two ways to generate the query. First: by defining a {@code 
   * dataSource}. Second: describing the fields manually. Only one at an 
   * instance of the SqlInsert object is possible. 
   * 
   * @param si the SqlInsert object
   * @param paramMode sets the parameter mode
   * @return a generated SQL INSERT query
   */
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

    if (!BeeUtils.isEmpty(si.getDataSource())) {
      query.append(si.getDataSource().getSqlString(this, paramMode));
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

  /**
   * Generates an SQL SELECT query from the specified argument {@code ss}.
   * From value must be defined in order to generate the query. 
   * 
   * @param ss the SqlSelect object
   * @param paramMode sets the parameter mode
   * @return a generated SQL SELECT query
   */
  String getQuery(SqlSelect ss, boolean paramMode) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    StringBuilder query = new StringBuilder("SELECT ");

    if (ss.isDistinctMode()) {
      query.append("DISTINCT ");
    }
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
      query.append(from.getSqlString(this, paramMode));
    }
    IsCondition whereClause = ss.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      String wh = whereClause.getSqlString(this, paramMode);

      if (!BeeUtils.isEmpty(wh)) {
        query.append(" WHERE ").append(wh);
      }
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
    IsCondition havingClause = ss.getHaving();

    if (!BeeUtils.isEmpty(havingClause)) {
      query.append(" HAVING ")
          .append(havingClause.getSqlString(this, paramMode));
    }
    List<SqlSelect> unionList = ss.getUnion();

    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        query.append(ss.isUnionAllMode() ? " UNION ALL " : " UNION ")
            .append("(").append(union.getSqlString(this, paramMode)).append(")");
      }
    }
    List<String[]> orderList = ss.getOrderBy();

    if (!BeeUtils.isEmpty(orderList)) {
      query.append(" ORDER BY ");

      for (int i = 0; i < orderList.size(); i++) {
        if (i > 0) {
          query.append(", ");
        }
        String[] orderEntry = orderList.get(i);
        IsExpression order = BeeUtils.isEmpty(ss.getUnion())
            ? SqlUtils.field(orderEntry[SqlSelect.ORDER_SRC], orderEntry[SqlSelect.ORDER_FLD])
            : SqlUtils.name(orderEntry[SqlSelect.ORDER_FLD]);

        query.append(order.getSqlString(this, paramMode))
            .append(orderEntry[SqlSelect.ORDER_DESC]);
      }
    }
    return query.toString();
  }

  /**
   * Generates an SQL UPDATE query from the specified argument {@code su}.
   * A target table and at least one expression must be defined.
   * 
   * @param su the SqlUpdate object.
   * @param paramMode sets teh parameter mode
   * @return a generated SQL UPDATE query
   */
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
      IsExpression field = updateEntry[SqlUpdate.FIELD];
      query.append(field.getSqlString(this, paramMode));

      IsExpression value = updateEntry[SqlUpdate.VALUE];
      query.append("=").append(value.getSqlString(this, paramMode));
    }
    List<IsFrom> fromList = su.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getSqlString(this, paramMode));
      }
    }
    IsCondition whereClause = su.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      String wh = whereClause.getSqlString(this, paramMode);

      if (!BeeUtils.isEmpty(wh)) {
        query.append(" WHERE ").append(wh);
      }
    }
    return query.toString();
  }
}
