package com.butent.bee.server.data;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.jdbc.JdbcException;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.ejb.Stateless;

/**
 * Is used by rpc_db_meta service requests handler, returns parameters of <code>ResultSets</code>.
 */

@Stateless
public class ResultSetBean {

  private static final BeeLogger logger = LogUtils.getLogger(ResultSetBean.class);
  private static final BeeColumn[] metaCols;

  static {
    String[] arr = new String[] {
        "index", "name", "label",
        "schema", "catalog", "table",
        "class", "sql type", "type name", "value type",
        "precision", "scale", "nullable",
        "display size", "signed", "auto increment", "case sensitive", "currency",
        "searchable", "read only", "writable", "definitely writable"};

    metaCols = new BeeColumn[arr.length];
    for (int i = 0; i < arr.length; i++) {
      metaCols[i] = new BeeColumn(arr[i]);
    }
  }

  public void rsMdToResponse(ResultSet rs, ResponseBuffer buff, boolean debug) {
    Assert.noNulls(rs, buff);
    DateTime start = new DateTime();

    for (int i = 0; i < metaCols.length; i++) {
      buff.addColumn(metaCols[i]);
    }
    if (debug) {
      buff.addColumn(new BeeColumn(start.toTimeString()));
    }

    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 1; i <= rsmd.getColumnCount(); i++) {
        int sqlType = rsmd.getColumnType(i);

        buff.addRow(i, rsmd.getColumnName(i), rsmd.getColumnLabel(i),
            rsmd.getSchemaName(i), rsmd.getCatalogName(i), rsmd.getTableName(i),
            rsmd.getColumnClassName(i), sqlType, rsmd.getColumnTypeName(i),
            JdbcUtils.sqlTypeToValueType(sqlType),
            rsmd.getPrecision(i), rsmd.getScale(i), rsmd.isNullable(i),
            rsmd.getColumnDisplaySize(i), rsmd.isSigned(i), rsmd.isAutoIncrement(i),
            rsmd.isCaseSensitive(i), rsmd.isCurrency(i), rsmd.isSearchable(i),
            rsmd.isReadOnly(i), rsmd.isWritable(i), rsmd.isDefinitelyWritable(i));
        if (debug) {
          buff.add(new DateTime().toTimeString());
        }
      }
    } catch (SQLException ex) {
      logger.error(ex);
      buff.addError(ex);
    }
  }

  public void rsToResponse(ResultSet rs, ResponseBuffer buff, boolean debug) {
    Assert.noNulls(rs, buff);
    DateTime start = new DateTime();

    BeeColumn[] cols = null;
    int c;

    try {
      cols = JdbcUtils.getColumns(rs);
      c = cols.length;
    } catch (JdbcException ex) {
      logger.error(ex);
      buff.addError(ex);
      c = 0;
    }

    if (c <= 0) {
      buff.addSevere("Cannot get result set meta data");
      return;
    }

    for (int i = 0; i < c; i++) {
      buff.addColumn(cols[i]);
    }

    if (debug) {
      buff.addColumn(new BeeColumn(start.toTimeString()));
    }

    try {
      while (rs.next()) {
        for (int i = 0; i < c; i++) {
          buff.add(rs.getString(cols[i].getIndex()));
        }
        if (debug) {
          buff.add(new DateTime().toTimeString());
        }
      }
    } catch (SQLException ex) {
      logger.error(ex);
      buff.addError(ex);
      buff.clearData();
    }
  }
}
