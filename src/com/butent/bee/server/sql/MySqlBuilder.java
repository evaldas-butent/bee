package com.butent.bee.server.sql;

import com.butent.bee.server.sql.BeeConstants.DataType;
import com.butent.bee.server.sql.BeeConstants.Function;
import com.butent.bee.server.sql.BeeConstants.SqlKeyword;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

/**
 * Contains specific requirements for SQL statement building for MySQL server.
 */

class MySqlBuilder extends SqlBuilder {

  @Override
  protected String sqlFunction(Function function, Map<String, Object> params) {
    switch (function) {
      case CAST:
        String sql = "CAST(" + params.get("expression");
        String dataType;
        DataType type = (DataType) params.get("type");
        int precision = (Integer) params.get("precision");
        int scale = (Integer) params.get("scale");

        switch (type) {
          case BOOLEAN:
            dataType = "DECIMAL(1)";
            break;

          case INTEGER:
          case DATE:
            dataType = "DECIMAL(10)";
            break;

          case LONG:
          case DATETIME:
            dataType = "DECIMAL(19)";
            break;

          case DOUBLE:
            dataType = "DECIMAL(65, 30)";
            break;

          case DECIMAL:
            dataType = "DECIMAL(" + precision + ", " + scale + ")";
            break;

          case STRING:
            dataType = "CHAR(" + precision + ")";
            break;

          default:
            dataType = super.sqlType(type, precision, scale);
        }
        return BeeUtils.concat(1, sql, "AS", dataType + ")");

      default:
        return super.sqlFunction(function, params);
    }
  }

  @Override
  protected String sqlKeyword(SqlKeyword option, Map<String, Object> params) {
    switch (option) {
      case DB_SCHEMA:
        return "SELECT schema() AS " + sqlQuote("dbSchema");

      case DB_TABLES:
        String sql = "SHOW TABLES";

        Object prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          sql = BeeUtils.concat(1, sql, "IN", sqlQuote((String) prm));
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          sql = BeeUtils.concat(1, sql, "LIKE", sqlTransform(prm));
        }
        return sql;

      case DB_FOREIGNKEYS:
        IsCondition wh = null;

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
          wh = SqlUtils.and(wh, SqlUtils.equal("c", "referenced_table_name", prm));
        }
        return new SqlSelect()
            .addField("c", "constraint_name", BeeConstants.KEY_NAME)
            .addField("t", "table_name", BeeConstants.TBL_NAME)
            .addField("c", "referenced_table_name", BeeConstants.FK_REF_TABLE)
            .addFrom("information_schema.referential_constraints", "c")
            .addFromInner("information_schema.table_constraints", "t",
                SqlUtils.joinUsing("c", "t", "constraint_name"))
            .setWhere(wh)
            .getQuery(this);

      case DROP_FOREIGNKEY:
        return BeeUtils.concat(1,
            "ALTER TABLE", params.get("table"),
            "DROP FOREIGN KEY", params.get("name"));

      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    return "`" + value + "`";
  }

  @Override
  protected String sqlTransform(Object x) {
    Object val;

    if (x instanceof Value) {
      val = ((Value) x).getObjectValue();
    } else {
      val = x;
    }
    String s = super.sqlTransform(val);

    if (val instanceof CharSequence) {
      s = s.replace("\\", "\\\\");
    }
    return s;
  }

  @Override
  String getCreate(SqlCreate sc, boolean paramMode) {
    String sql = super.getCreate(sc, paramMode);

    if (BeeUtils.isEmpty(sc.getDataSource())) {
      sql += " ENGINE=InnoDB";
    }
    return sql;
  }

  @Override
  String getQuery(SqlSelect ss, boolean paramMode) {
    int limit = ss.getLimit();
    int offset = ss.getOffset();
    String sql = super.getQuery(ss, paramMode);

    if (BeeUtils.isPositive(limit)) {
      sql += " LIMIT " + limit;
    } else if (BeeUtils.isPositive(offset)) {
      sql += " LIMIT " + (int) 1e9;
    }
    if (BeeUtils.isPositive(offset)) {
      sql += " OFFSET " + offset;
    }
    return sql;
  }
}
