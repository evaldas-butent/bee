package com.butent.bee.egg.server.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.server.jdbc.JdbcException;
import com.butent.bee.egg.server.jdbc.JdbcUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeColumn;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

@Stateless
public class ResultSetBean {
  private static final Logger logger = Logger.getLogger(ResultSetBean.class
      .getName());
  private static final BeeColumn[] metaCols;

  static {
    String[] arr = new String[] { "index", "name", "schema", "catalog",
        "table", "class", "type", "type name", "label", "display size",
        "precision", "scale", "nullable", "signed", "auto increment",
        "case sensitive", "currency", "searchable", "read only", "writable",
        "definitely writable" };

    metaCols = new BeeColumn[arr.length];
    for (int i = 0; i < arr.length; i++)
      metaCols[i] = new BeeColumn(arr[i]);
  }

  public void rsToResponse(ResultSet rs, ResponseBuffer buff, boolean debug) {
    Assert.noNulls(rs, buff);
    BeeDate start = new BeeDate();

    BeeColumn[] cols = null;
    int c;
    
    try {
      cols = JdbcUtils.getColumns(rs);
      c = cols.length;
    }
    catch (JdbcException ex) {
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

    if (debug)
      buff.addColumn(new BeeColumn(start.toLog()));

    try {
      while (rs.next()) {
        for (int i = 0; i < c; i++)
          buff.add(rs.getString(cols[i].getIdx()));
        if (debug)
          buff.add(new BeeDate().toLog());
      }
    } catch (SQLException ex) {
      LogUtils.error(logger, ex);
      buff.addError(ex);
      buff.clearData();
    }
  }

  public void rsMdToResponse(ResultSet rs, ResponseBuffer buff, boolean debug) {
    Assert.noNulls(rs, buff);
    BeeDate start = new BeeDate();

    BeeColumn[] cols = null;
    int c;
    
    try {
      cols = JdbcUtils.getColumns(rs);
      c = cols.length;
    }
    catch (JdbcException ex) {
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
    if (debug)
      buff.addColumn(new BeeColumn(start.toLog()));

    BeeColumn z;

    for (int i = 0; i < c; i++) {
      z = cols[i];

      buff.add(z.getIdx(), z.getName(), z.getSchema(), z.getCatalog(),
          z.getTable(), z.getClazz(), z.getType(), z.getTypeName(),
          z.getLabel(), z.getDisplaySize(), z.getPrecision(), z.getScale(),
          BeeUtils.concat(1, z.getNullable(), z.nullableAsString()),
          z.isSigned(), z.isAutoIncrement(), z.isCaseSensitive(),
          z.isCurrency(), z.isSearchable(), z.isReadOnly(), z.isWritable(),
          z.isDefinitelyWritable());

      if (debug)
        buff.add(new BeeDate().toLog());
    }
  }
}
