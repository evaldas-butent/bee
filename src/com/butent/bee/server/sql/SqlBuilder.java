package com.butent.bee.server.sql;

import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlConstants.SqlFunction;
import com.butent.bee.server.sql.SqlConstants.SqlKeyword;
import com.butent.bee.server.sql.SqlCreate.SqlField;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Is an abstract class for all SQL server specific SQL builders, contains core requirements for SQL
 * statements.
 */

public abstract class SqlBuilder {

  final String getQuery(IsQuery query) {
    if (query instanceof SqlCreate) {
      return getCreate((SqlCreate) query);

    } else if (query instanceof SqlDelete) {
      return getDelete((SqlDelete) query);

    } else if (query instanceof SqlInsert) {
      return getInsert((SqlInsert) query);

    } else if (query instanceof SqlSelect) {
      return getSelect((SqlSelect) query);

    } else if (query instanceof SqlUpdate) {
      return getUpdate((SqlUpdate) query);

    } else {
      Assert.unsupported("Unsupported class name: " + BeeUtils.getClassName(query.getClass()));
    }
    return null;
  }

  /**
   * Generates an SQL CREATE query from the specified argument {@code sc}. There are two ways to
   * generate the query. First: by defining a {@code  dataSource}. Second: describing the fields
   * manually. Only one at an instance of the SqlCreate object is possible.
   * 
   * @param sc the SqlCreate object
   * @return a generated SQL CREATE query
   */
  protected String getCreate(SqlCreate sc) {
    Assert.notNull(sc);
    Assert.state(!sc.isEmpty());

    StringBuilder query = new StringBuilder("CREATE ");

    if (sc.isTemporary()) {
      query.append(sqlKeyword(SqlKeyword.TEMPORARY, null));
    }
    query.append("TABLE ");

    query.append(sc.getTarget().getSqlString(this));

    List<SqlField> fieldList = sc.getFields();

    if (!BeeUtils.isEmpty(sc.getDataSource())) {
      query.append(" AS ").append(sc.getDataSource().getSqlString(this));
    } else {
      query.append(" (");

      for (int i = 0; i < fieldList.size(); i++) {
        if (i > 0) {
          query.append(", ");
        }
        SqlField field = fieldList.get(i);
        query.append(field.getName().getSqlString(this))
            .append(" ").append(sqlType(field.getType(), field.getPrecision(), field.getScale()));

        if (field.isNotNull()) {
          query.append(" NOT NULL");
        }
      }
      query.append(")");
    }
    return query.toString();
  }

  /**
   * Generates an SQL DELETE query from the specified argument {@code sd}. {@code sd} must have From
   * and Where conditions set.
   * 
   * @param sd the SqlDelete object to use for generating
   * @return a generated SQL DELETE query
   */
  protected String getDelete(SqlDelete sd) {
    Assert.notNull(sd);
    Assert.state(!sd.isEmpty());

    StringBuilder query = new StringBuilder("DELETE FROM ");

    query.append(sd.getTarget().getSqlString(this));

    List<IsFrom> fromList = sd.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getSqlString(this));
      }
    }
    String wh = sd.getWhere().getSqlString(this);

    if (!BeeUtils.isEmpty(wh)) {
      query.append(" WHERE ").append(wh);
    }
    return query.toString();
  }

  /**
   * Generates an SQL INSERT query from the specified argument {@code si}. There are two ways to
   * generate the query. First: by defining a {@code  dataSource}. Second: describing the fields
   * manually. Only one at an instance of the SqlInsert object is possible.
   * 
   * @param si the SqlInsert object
   * @return a generated SQL INSERT query
   */
  protected String getInsert(SqlInsert si) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());

    StringBuilder query = new StringBuilder("INSERT INTO ");

    query.append(si.getTarget().getSqlString(this));

    List<IsExpression> fieldList = si.getFields();

    query.append(" (");

    for (int i = 0; i < fieldList.size(); i++) {
      if (i > 0) {
        query.append(", ");
      }
      IsExpression field = fieldList.get(i);
      query.append(field.getSqlString(this));
    }
    query.append(") ");

    if (!BeeUtils.isEmpty(si.getDataSource())) {
      query.append(si.getDataSource().getSqlString(this));
    } else {
      List<IsExpression> valueList = si.getValues();

      if (!BeeUtils.isEmpty(valueList)) {
        query.append("VALUES (");

        for (int i = 0; i < valueList.size(); i++) {
          if (i > 0) {
            query.append(", ");
          }
          IsExpression value = valueList.get(i);
          query.append(value.getSqlString(this));
        }
        query.append(")");
      }
    }
    return query.toString();
  }

  /**
   * Generates an SQL SELECT query from the specified argument {@code ss}. From value must be
   * defined in order to generate the query.
   * 
   * @param ss the SqlSelect object
   * @return a generated SQL SELECT query
   */
  protected String getSelect(SqlSelect ss) {
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
      query.append(field.getSqlString(this));

      IsExpression alias = fldEntry[SqlSelect.FIELD_ALIAS];

      if (!BeeUtils.isEmpty(alias)) {
        query.append(" AS ").append(alias.getSqlString(this));
      }
    }
    List<IsFrom> fromList = ss.getFrom();

    query.append(" FROM ");

    for (IsFrom from : fromList) {
      query.append(from.getSqlString(this));
    }
    IsCondition whereClause = ss.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      String wh = whereClause.getSqlString(this);

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
        String group = groupList.get(i).getSqlString(this);
        query.append(group);
      }
    }
    IsCondition havingClause = ss.getHaving();

    if (!BeeUtils.isEmpty(havingClause)) {
      query.append(" HAVING ")
          .append(havingClause.getSqlString(this));
    }
    List<SqlSelect> unionList = ss.getUnion();

    if (!BeeUtils.isEmpty(unionList)) {
      for (SqlSelect union : unionList) {
        query.append(ss.isUnionAllMode() ? " UNION ALL " : " UNION ")
            .append("(").append(union.getSqlString(this)).append(")");
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

        query.append(order.getSqlString(this)).append(orderEntry[SqlSelect.ORDER_DESC]);
      }
    }
    return query.toString();
  }

  /**
   * Generates an SQL UPDATE query from the specified argument {@code su}. A target table and at
   * least one expression must be defined.
   * 
   * @param su the SqlUpdate object.
   * @return a generated SQL UPDATE query
   */
  protected String getUpdate(SqlUpdate su) {
    Assert.notNull(su);
    Assert.state(!su.isEmpty());

    StringBuilder query = new StringBuilder("UPDATE ");

    query.append(su.getTarget().getSqlString(this));

    List<IsExpression[]> updates = su.getUpdates();

    query.append(" SET ");

    for (int i = 0; i < updates.size(); i++) {
      if (i > 0) {
        query.append(", ");
      }
      IsExpression[] updateEntry = updates.get(i);
      IsExpression field = updateEntry[SqlUpdate.FIELD];
      query.append(field.getSqlString(this));

      IsExpression value = updateEntry[SqlUpdate.VALUE];
      query.append("=").append(value.getSqlString(this));
    }
    List<IsFrom> fromList = su.getFrom();

    if (!BeeUtils.isEmpty(fromList)) {
      query.append(" FROM ");

      for (IsFrom from : fromList) {
        query.append(from.getSqlString(this));
      }
    }
    IsCondition whereClause = su.getWhere();

    if (!BeeUtils.isEmpty(whereClause)) {
      String wh = whereClause.getSqlString(this);

      if (!BeeUtils.isEmpty(wh)) {
        query.append(" WHERE ").append(wh);
      }
    }
    return query.toString();
  }

  protected String sqlCondition(Operator operator, Map<String, String> params) {
    String expression = params.get("expression");
    String value = params.get("value" + 0);

    switch (operator) {
      case IS_NULL:
      case NOT_NULL:
        return BeeUtils.concat(1,
            expression, "IS", (operator == Operator.NOT_NULL ? "NOT" : ""), "NULL");

      case IN:
        return BeeUtils.concat(1, expression, "IN", value);

      case EQ:
      case NE:
      case LT:
      case GT:
      case LE:
      case GE:
        return BeeUtils.concat(1, expression, operator.toTextString(), value);

      case STARTS:
      case ENDS:
      case CONTAINS:
      case MATCHES:
        value = value.replace("|", "||").replace("%", "|%").replace("_", "|_");

        if (operator == Operator.MATCHES) {
          value = value.replace(Operator.CHAR_ANY, "%").replace(Operator.CHAR_ONE, "_");
        } else {
          value = value.replaceFirst("^(" + sqlTransform(")(.*)(") + ")$",
              "$1" + (operator != Operator.STARTS ? "%" : "")
                  + "$2" + (operator != Operator.ENDS ? "%" : "") + "$3");
        }
        return BeeUtils.concat(1, expression, "LIKE", value, "ESCAPE '|'");
    }
    Assert.untouchable();
    return null;
  }

  protected String sqlFunction(SqlFunction function, Map<String, Object> params) {
    switch (function) {
      case BITAND:
        return "(" + params.get("expression") + " & " + params.get("value") + ")";

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
            sqlType((SqlDataType) params.get("type"),
                (Integer) params.get("precision"),
                (Integer) params.get("scale")) + ")");

      case MIN:
      case MAX:
      case SUM:
      case AVG:
      case COUNT:
        String expression = (String) params.get("expression");
        if (BeeUtils.isEmpty(expression)) {
          expression = "*";
        }
        return function +
            "(" + ((Boolean) params.get("distinct") ? "DISTINCT " : "") + expression + ")";

      case PLUS:
      case MINUS:
      case MULTIPLY:
      case DIVIDE:
      case BULK:
        xpr = new StringBuilder(BeeUtils.transform(params.get("member" + 0)));
        String op;

        switch (function) {
          case PLUS:
            op = "+";
            break;
          case MINUS:
            op = "-";
            break;
          case MULTIPLY:
            op = "*";
            break;
          case DIVIDE:
            op = "/";
            break;
          default:
            op = "";
            break;
        }
        for (int i = 1; i < params.size(); i++) {
          xpr.append(op).append(params.get("member" + i));
        }
        return function != SqlFunction.BULK
            ? BeeUtils.parenthesize(xpr.toString()) : xpr.toString();
    }
    Assert.untouchable();
    return null;
  }

  protected String sqlKeyword(SqlKeyword option, Map<String, Object> params) {
    switch (option) {
      case CREATE_INDEX:
        return BeeUtils.concat(1,
            "CREATE INDEX", params.get("name"),
            "ON", params.get("table"),
            BeeUtils.parenthesize(params.get("fields")));

      case ADD_CONSTRAINT:
        return BeeUtils.concat(1,
            "ALTER TABLE", params.get("table"),
            "ADD CONSTRAINT", params.get("name"),
            sqlKeyword((SqlKeyword) params.get("type"), params));

      case PRIMARY_KEY:
        return BeeUtils.concat(1, "PRIMARY KEY", BeeUtils.parenthesize(params.get("fields")));

      case UNIQUE_KEY:
        return BeeUtils.concat(1, "UNIQUE", BeeUtils.parenthesize(params.get("fields")));

      case FOREIGN_KEY:
        String foreign = BeeUtils.concat(1,
            "FOREIGN KEY", BeeUtils.parenthesize(params.get("fields")),
            "REFERENCES", params.get("refTable"), BeeUtils.parenthesize(params.get("refFields")));

        if (!BeeUtils.isEmpty(params.get("cascade"))) {
          foreign = BeeUtils.concat(1,
              foreign, "ON DELETE",
              BeeUtils.isEmpty(params.get("cascadeDelete")) ? "SET NULL" : "CASCADE");
        }
        return foreign;

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
            .getSqlString(this);

      case DB_FIELDS:
        wh = null;

        prm = params.get("dbName");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.equal("c", "table_catalog", prm);
        }
        prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("c", "table_schema", prm));
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("c", "table_name", prm));
        }
        return new SqlSelect()
            .addField("c", "table_name", SqlConstants.TBL_NAME)
            .addField("c", "column_name", SqlConstants.FLD_NAME)
            .addField("c", "is_nullable", SqlConstants.FLD_NULL)
            .addField("c", "data_type", SqlConstants.FLD_TYPE)
            .addField("c", "character_maximum_length", SqlConstants.FLD_LENGTH)
            .addField("c", "numeric_precision", SqlConstants.FLD_PRECISION)
            .addField("c", "numeric_scale", SqlConstants.FLD_SCALE)
            .addFrom("information_schema.columns", "c")
            .setWhere(wh)
            .addOrder("c", "ordinal_position")
            .getSqlString(this);

      case DB_KEYS:
        wh = null;

        prm = params.get("dbName");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.equal("k", "constraint_catalog", prm);
        }
        prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("k", "constraint_schema", prm));
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("k", "table_name", prm));
        }
        prm = params.get("keyTypes");
        if (!BeeUtils.isEmpty(prm)) {
          IsCondition typeWh = null;

          for (SqlKeyword type : (SqlKeyword[]) prm) {
            String tp;

            switch (type) {
              case PRIMARY_KEY:
                tp = "PRIMARY KEY";
                break;

              case UNIQUE_KEY:
                tp = "UNIQUE";
                break;

              case FOREIGN_KEY:
                tp = "FOREIGN KEY";
                break;

              default:
                tp = null;
            }
            if (!BeeUtils.isEmpty(tp)) {
              typeWh = SqlUtils.or(typeWh, SqlUtils.equal("k", "constraint_type", tp));
            }
          }
          if (!BeeUtils.isEmpty(typeWh)) {
            wh = SqlUtils.and(wh, typeWh);
          }
        }
        return new SqlSelect()
            .addField("k", "table_name", SqlConstants.TBL_NAME)
            .addField("k", "constraint_name", SqlConstants.KEY_NAME)
            .addField("k", "constraint_type", SqlConstants.KEY_TYPE)
            .addFrom("information_schema.table_constraints", "k")
            .setWhere(wh)
            .getSqlString(this);

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
            .addField("c", "constraint_name", SqlConstants.KEY_NAME)
            .addField("t", "table_name", SqlConstants.TBL_NAME)
            .addField("r", "table_name", SqlConstants.FK_REF_TABLE)
            .addFrom("information_schema.referential_constraints", "c")
            .addFromInner("information_schema.table_constraints", "t",
                SqlUtils.joinUsing("c", "t", "constraint_name"))
            .addFromInner("information_schema.table_constraints", "r",
                SqlUtils.join("c", "unique_constraint_name", "r", "constraint_name"))
            .setWhere(wh)
            .getSqlString(this);

      case DROP_TABLE:
        return "DROP TABLE " + params.get("table");

      case DROP_FOREIGNKEY:
        return BeeUtils.concat(1,
            "ALTER TABLE", params.get("table"),
            "DROP CONSTRAINT", params.get("name"));

      case RENAME_TABLE:
        return BeeUtils.concat(1,
            "ALTER TABLE", params.get("nameFrom"), "RENAME TO", params.get("nameTo"));

      case TEMPORARY:
        return "TEMPORARY ";

      case TEMPORARY_NAME:
        return (String) params.get("name");
    }
    Assert.untouchable();
    return null;
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

  protected String sqlType(SqlDataType type, int precision, int scale) {
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
      case DECIMAL:
        return "NUMERIC(" + precision + ", " + scale + ")";
      case CHAR:
        return "CHAR(" + precision + ")";
      case STRING:
        return "VARCHAR(" + precision + ")";
    }
    Assert.untouchable();
    return null;
  }
}
