package com.butent.bee.egg.server.jdbc;

import java.lang.reflect.Field;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.butent.bee.egg.server.utils.BeeResultSet;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeColumn;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;

public abstract class JdbcUtils {
  private static final Logger logger = Logger.getLogger(JdbcUtils.class
      .getName());

  public static String getJdbcTypeName(int jdbcType) {
    String name = null;
    Field[] fields = java.sql.Types.class.getFields();

    for (int i = 0; i < fields.length; i++) {
      try {
        if (fields[i].getInt(null) == jdbcType) {
          name = fields[i].getName();
          break;
        }
      } catch (IllegalArgumentException ex) {
        name = ex.getMessage();
      } catch (IllegalAccessException ex) {
        name = ex.getMessage();
      }
    }

    return name;
  }

  public static int getSize(ResultSet rs) {
    int c = 0;
    if (rs == null)
      return c;

    try {
      if (rs.getType() == ResultSet.TYPE_FORWARD_ONLY)
        while (rs.next())
          c++;
      else if (rs.last())
        c = rs.getRow();
    } catch (SQLException ex) {
      c = BeeConst.SIZE_UNKNOWN;
    }

    return c;
  }

  public static int getColumnCount(Object obj) throws JdbcException {
    Assert.notNull(obj);

    int c = BeeConst.SIZE_UNKNOWN;

    try {
      if (obj instanceof ResultSetMetaData)
        c = ((ResultSetMetaData) obj).getColumnCount();
      else if (obj instanceof ResultSet)
        c = ((ResultSet) obj).getMetaData().getColumnCount();
      else if (obj instanceof BeeResultSet)
        c = ((BeeResultSet) obj).getColumnCount();
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return c;
  }

  public static boolean validColumnIdx(ResultSetMetaData rsmd, int idx)
      throws JdbcException {
    if (rsmd == null)
      return false;
    else
      return idx >= 1 && idx <= getColumnCount(rsmd);
  }

  public static boolean setColumnInfo(ResultSetMetaData rsmd, int idx,
      BeeColumn col) throws JdbcException {
    Assert.notNull(rsmd);
    Assert.isPositive(idx);
    Assert.notNull(col);

    if (!validColumnIdx(rsmd, idx))
      return false;

    boolean ok = false;

    try {
      col.setIdx(idx);

      col.setSchema(rsmd.getSchemaName(idx));
      col.setCatalog(rsmd.getCatalogName(idx));
      col.setTable(rsmd.getTableName(idx));

      col.setName(rsmd.getColumnName(idx));
      col.setType(rsmd.getColumnType(idx));
      col.setTypeName(rsmd.getColumnTypeName(idx));
      col.setClazz(rsmd.getColumnClassName(idx));

      col.setPrecision(rsmd.getPrecision(idx));
      col.setScale(rsmd.getScale(idx));

      col.setNullable(rsmd.isNullable(idx));

      col.setDisplaySize(rsmd.getColumnDisplaySize(idx));
      col.setLabel(rsmd.getColumnLabel(idx));

      col.setAutoIncrement(rsmd.isAutoIncrement(idx));
      col.setCaseSensitive(rsmd.isCaseSensitive(idx));
      col.setCurrency(rsmd.isCurrency(idx));
      col.setSigned(rsmd.isSigned(idx));

      col.setDefinitelyWritable(rsmd.isDefinitelyWritable(idx));
      col.setReadOnly(rsmd.isReadOnly(idx));
      col.setSearchable(rsmd.isSearchable(idx));
      col.setWritable(rsmd.isWritable(idx));

      ok = true;
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return ok;
  }

  public static String rsTypeAsString(int z) {
    switch (z) {
    case (ResultSet.TYPE_FORWARD_ONLY):
      return BeeConst.TYPE_FORWARD_ONLY;
    case (ResultSet.TYPE_SCROLL_INSENSITIVE):
      return BeeConst.TYPE_SCROLL_INSENSITIVE;
    case (ResultSet.TYPE_SCROLL_SENSITIVE):
      return BeeConst.TYPE_SCROLL_SENSITIVE;
    default:
      return BeeConst.UNKNOWN;
    }
  }

  public static String concurrencyAsString(int z) {
    switch (z) {
    case (ResultSet.CONCUR_READ_ONLY):
      return BeeConst.CONCUR_READ_ONLY;
    case (ResultSet.CONCUR_UPDATABLE):
      return BeeConst.CONCUR_UPDATABLE;
    default:
      return BeeConst.UNKNOWN;
    }
  }

  public static String holdabilityAsString(int z) {
    switch (z) {
    case (ResultSet.CLOSE_CURSORS_AT_COMMIT):
      return BeeConst.CLOSE_CURSORS_AT_COMMIT;
    case (ResultSet.HOLD_CURSORS_OVER_COMMIT):
      return BeeConst.HOLD_CURSORS_OVER_COMMIT;
    default:
      return BeeConst.UNKNOWN;
    }
  }

  public static String fetchDirectionAsString(int z) {
    switch (z) {
    case (ResultSet.FETCH_FORWARD):
      return BeeConst.FETCH_FORWARD;
    case (ResultSet.FETCH_REVERSE):
      return BeeConst.FETCH_REVERSE;
    case (ResultSet.FETCH_UNKNOWN):
      return BeeConst.FETCH_UNKNOWN;
    default:
      return BeeConst.UNKNOWN;
    }
  }

  public static String[] getColNames(ResultSet rs) {
    Assert.notNull(rs);

    int c;
    String[] arr = null;

    try {
      ResultSetMetaData md = rs.getMetaData();
      c = md.getColumnCount();

      if (c > 0) {
        arr = new String[c];
        for (int i = 0; i < c; i++)
          arr[i] = md.getColumnName(i + 1);
      }
    } catch (SQLException ex) {
      c = 0;
    }

    if (c > 0)
      return arr;
    else
      return null;
  }

  public static List<StringProp> getRs(ResultSet rs) {
    if (rs == null)
      return null;

    String[] cols = getColNames(rs);
    if (BeeUtils.isEmpty(cols))
      return null;

    List<StringProp> lst = new ArrayList<StringProp>();
    int r = 0;

    try {
      while (rs.next()) {
        r++;
        PropUtils.addString(lst, JdbcConst.ROW_ID, r);

        for (String nm : cols)
          PropUtils.addString(lst, nm, rs.getString(nm));
      }
    } catch (SQLException ex) {
      PropUtils.addString(lst, "Error", ex.getMessage());
    }

    return lst;
  }

  public static Connection getConnection(DataSource src) throws JdbcException {
    if (src == null)
      return null;
    Connection con = null;

    try {
      con = src.getConnection();
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return con;
  }

  public static void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (SQLException ex) {
        logger.log(Level.WARNING, "Could not close JDBC Connection", ex);
      } catch (Exception ex) {
        logger.log(Level.WARNING,
            "Unexpected exception on closing JDBC Connection", ex);
      }
    }
  }

  public static void closeStatement(Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException ex) {
        logger.log(Level.WARNING, "Could not close JDBC Statement", ex);
      } catch (Exception ex) {
        logger.log(Level.WARNING,
            "Unexpected exception on closing JDBC Statement", ex);
      }
    }
  }

  public static void closeResultSet(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException ex) {
        logger.log(Level.WARNING, "Could not close JDBC ResultSet", ex);
      } catch (Exception ex) {
        logger.log(Level.WARNING,
            "Unexpected exception on closing JDBC ResultSet", ex);
      }
    }
  }

  public static String requiredSingleResult(ResultSet rs) throws JdbcException {
    Assert.notNull(rs);
    String v = null;

    int size = 0;

    try {
      while (rs.next()) {
        size++;
        if (size == 1)
          v = rs.getString(1);
        else
          break;
      }
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    if (size == 0)
      throw new JdbcException(JdbcConst.RESULT_SET_EMPTY);
    else if (size > 1)
      throw new JdbcException(JdbcConst.rsRows(size));

    return v;
  }

  public static boolean supportsBatchUpdates(Connection con)
      throws JdbcException {
    Assert.notNull(con);

    boolean ok = false;

    try {
      DatabaseMetaData dbmd = con.getMetaData();
      ok = dbmd.supportsBatchUpdates();
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    } catch (AbstractMethodError err) {
      throw new JdbcException(
          "JDBC driver does not support 'supportsBatchUpdates' method", err);
    }

    return ok;
  }

  public static String lookupColumnName(ResultSetMetaData rsmd, int idx)
      throws JdbcException {
    Assert.notNull(rsmd);
    Assert.isPositive(idx);

    String name = null;

    try {
      name = rsmd.getColumnLabel(idx);
      if (BeeUtils.isEmpty(name))
        name = rsmd.getColumnName(idx);
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return name;
  }

  public static String getResultSetValue(ResultSet rs, int idx)
      throws JdbcException {
    Assert.notNull(rs);
    Assert.isPositive(idx);

    String v = null;

    try {
      v = rs.getString(idx);
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return v;
  }

  public static void applyTimeout(Statement stmt, int timeout)
      throws JdbcException {
    Assert.notNull(stmt);
    Assert.isPositive(timeout);

    try {
      stmt.setQueryTimeout(timeout);
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }
  }

  public static void applyFetchSize(Statement stmt, int rows)
      throws JdbcException {
    Assert.notNull(stmt);
    Assert.isPositive(rows);

    try {
      stmt.setFetchSize(rows);
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }
  }

  public static void applyMaxRows(Statement stmt, int rows)
      throws JdbcException {
    Assert.notNull(stmt);
    Assert.isPositive(rows);

    try {
      stmt.setMaxRows(rows);
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }
  }

  public static ResultSet getResultSet(Statement stmt) throws JdbcException {
    Assert.notNull(stmt);
    ResultSet rs = null;

    try {
      rs = stmt.getResultSet();
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return rs;
  }

  public static boolean getMoreResults(Statement stmt) throws JdbcException {
    Assert.notNull(stmt);
    boolean mr = false;

    try {
      mr = stmt.getMoreResults();
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return mr;
  }

  public static int getUpdateCount(Statement stmt) throws JdbcException {
    Assert.notNull(stmt);
    int cnt = BeeConst.SIZE_UNKNOWN;

    try {
      cnt = stmt.getUpdateCount();
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return cnt;
  }

  public static BeeColumn[] getColumns(ResultSet rs) throws JdbcException {
    Assert.notNull(rs);

    int c = getColumnCount(rs);
    Assert.isPositive(c);

    BeeColumn[] arr = new BeeColumn[c];

    try {
      ResultSetMetaData md = rs.getMetaData();
      for (int i = 0; i < c; i++) {
        arr[i] = new BeeColumn();
        setColumnInfo(md, i + 1, arr[i]);
      }
    } catch (SQLException ex) {
      throw new JdbcException(ex);
    }

    return arr;
  }

}
