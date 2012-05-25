package com.butent.bee.server.data;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.jdbc.JdbcException;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.LogUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.ejb.Stateless;

/**
 * Is used by rpc_db_meta service requests handler, returns parameters of <code>ResultSets</code>.
 */

@Stateless
public class ResultSetBean {

  private static final Logger logger = Logger.getLogger(ResultSetBean.class.getName());
  private static final BeeColumn[] metaCols;

  static {
    String[] arr = new String[] {
        "index", "id", "label", "schema", "catalog", "table", "class", "sql type",
        "type name", "type", "precision", "scale", "nullable", "pattern", "display size", "signed",
        "auto increment", "case sensitive", "currency", "searchable", "read only", "writable",
        "definitely writable", "properties"};

    metaCols = new BeeColumn[arr.length];
    for (int i = 0; i < arr.length; i++) {
      metaCols[i] = new BeeColumn(arr[i]);
    }
  }

  public void rsMdToResponse(ResultSet rs, ResponseBuffer buff, boolean debug) {
    Assert.noNulls(rs, buff);
    DateTime start = new DateTime();

    BeeColumn[] cols = null;
    int c;

    try {
      cols = JdbcUtils.getColumns(rs);
      c = cols.length;
    } catch (JdbcException ex) {
      LogUtils.error(logger, ex);
      buff.addError(ex);
      c = 0;
    }

    if (c <= 0) {
      buff.addSevere("Cannot get result set meta data");
      return;
    }

    for (int i = 0; i < metaCols.length; i++) {
      buff.addColumn(metaCols[i]);
    }
    if (debug) {
      buff.addColumn(new BeeColumn(start.toTimeString()));
    }

    BeeColumn z;
    for (int i = 0; i < c; i++) {
      z = cols[i];

      buff.add(z.getIndex(), z.getId(), z.getLabel(), z.getType(),
          z.getPrecision(), z.getScale(), z.isNullable(), z.getPattern(),
          z.isReadOnly(), z.getProperties());

      if (debug) {
        buff.add(new DateTime().toTimeString());
      }
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
      LogUtils.error(logger, ex);
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
      LogUtils.error(logger, ex);
      buff.addError(ex);
      buff.clearData();
    }
  }
}
