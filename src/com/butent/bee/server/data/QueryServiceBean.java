package com.butent.bee.server.data;

import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.IsQuery;
import com.butent.bee.shared.sql.SqlBuilderFactory;
import com.butent.bee.shared.sql.SqlInsert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class QueryServiceBean {

  private interface SqlHandler<T> {

    T processError(SQLException ex);

    T processResultSet(ResultSet rs) throws SQLException;

    T processUpdateCount(int updateCount);
  }

  private static Logger logger = Logger.getLogger(QueryServiceBean.class.getName());

  @EJB
  DataSourceBean dsb;
  @EJB
  IdGeneratorBean ig;
  @EJB
  SystemBean sys;

  public String[] dbFields(String table) {
    SimpleRowSet res = getData(new SqlSelect()
        .addAllFields(table).addFrom(table).setWhere(SqlUtils.sqlFalse()));
    Assert.notNull(res);

    return res.getColumnNames();
  }

  public SimpleRowSet dbForeignKeys(String dbName, String dbSchema, String table,
      String refTable) {
    return getData(SqlUtils.dbForeignKeys(dbName, dbSchema, table, refTable));
  }

  public String dbName() {
    IsQuery query = SqlUtils.dbName();

    if (!BeeUtils.isEmpty(query.getQuery())) {
      return getValue(query);
    }
    return "";
  }

  public int dbRowCount(String source, IsCondition where) {
    return dbRowCount(new SqlSelect().addConstant(1, "dummy").addFrom(source).setWhere(where));
  }

  public int dbRowCount(SqlSelect ss) {
    SimpleRowSet res = getData(new SqlSelect().addCount("cnt").addFrom(ss, "als"));

    if (res == null) {
      return -1;
    }
    return res.getInt(0, 0);
  }

  public String dbSchema() {
    IsQuery query = SqlUtils.dbSchema();

    if (!BeeUtils.isEmpty(query.getQuery())) {
      return getValue(query);
    }
    return "";
  }

  public String[] dbTables(String dbName, String dbSchema, String table) {
    return getColumn(SqlUtils.dbTables(dbName, dbSchema, table));
  }

  public Object doSql(String sql) {
    Assert.notEmpty(sql);

    return processSql(sql, new SqlHandler<Object>() {
      @Override
      public Object processError(SQLException ex) {
        LogUtils.error(logger, ex);
        return ex.getMessage();
      }

      @Override
      public Object processResultSet(ResultSet rs) throws SQLException {
        return rsToBeeRowSet(rs, null);
      }

      @Override
      public Object processUpdateCount(int updateCount) {
        LogUtils.info(logger, "Affected rows:", updateCount);
        return updateCount;
      }
    });
  }

  public boolean getBoolean(IsQuery query) {
    return getSingleValue(query).getBoolean(0, 0);
  }

  public boolean[] getBooleanColumn(IsQuery query) {
    return getSingleColumn(query).getBooleanColumn(0);
  }

  public String[] getColumn(IsQuery query) {
    return getSingleColumn(query).getColumn(0);
  }

  public SimpleRowSet getData(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    activateTables(query);

    return processSql(query.getQuery(), new SqlHandler<SimpleRowSet>() {
      @Override
      public SimpleRowSet processError(SQLException ex) {
        LogUtils.error(logger, ex);
        return null;
      }

      @Override
      public SimpleRowSet processResultSet(ResultSet rs) throws SQLException {
        return rsToSimpleRowSet(rs);
      }

      @Override
      public SimpleRowSet processUpdateCount(int updateCount) {
        LogUtils.warning(logger, "Query must return a ResultSet");
        return null;
      }
    });
  }

  public JustDate getDate(IsQuery query) {
    return getSingleValue(query).getDate(0, 0);
  }

  public JustDate[] getDateColumn(IsQuery query) {
    return getSingleColumn(query).getDateColumn(0);
  }

  public DateTime getDateTime(IsQuery query) {
    return getSingleValue(query).getDateTime(0, 0);
  }

  public DateTime[] getDateTimeColumn(IsQuery query) {
    return getSingleColumn(query).getDateTimeColumn(0);
  }

  public BigDecimal getDecimal(IsQuery query) {
    return getSingleValue(query).getDecimal(0, 0);
  }

  public BigDecimal[] getDecimalColumn(IsQuery query) {
    return getSingleColumn(query).getDecimalColumn(0);
  }

  public double getDouble(IsQuery query) {
    return getSingleValue(query).getDouble(0, 0);
  }

  public double[] getDoubleColumn(IsQuery query) {
    return getSingleColumn(query).getDoubleColumn(0);
  }

  public int getInt(IsQuery query) {
    return getSingleValue(query).getInt(0, 0);
  }

  public int[] getIntColumn(IsQuery query) {
    return getSingleColumn(query).getIntColumn(0);
  }

  public long getLong(IsQuery query) {
    return getSingleValue(query).getLong(0, 0);
  }

  public long[] getlongColumn(IsQuery query) {
    return getSingleColumn(query).getLongColumn(0);
  }

  public Map<String, String> getRow(IsQuery query) {
    SimpleRowSet res = getSingleRow(query);

    if (BeeUtils.isEmpty(res.getNumberOfRows())) {
      return null;
    }
    return res.getRow(0);
  }

  public String getValue(IsQuery query) {
    return getSingleValue(query).getValue(0, 0);
  }

  public String[] getValues(IsQuery query) {
    SimpleRowSet res = getSingleRow(query);

    if (BeeUtils.isEmpty(res.getNumberOfRows())) {
      return null;
    }
    return res.getValues(0);
  }

  public BeeRowSet getViewData(IsQuery query, final BeeView view) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    activateTables(query);

    if (!BeeUtils.isEmpty(view)) {
      Assert.state(query instanceof SqlSelect);
      String tableName = view.getSource();

      ((SqlSelect) query)
          .addFields(tableName, sys.getIdName(tableName), sys.getLockName(tableName));
    }
    return processSql(query.getQuery(), new SqlHandler<BeeRowSet>() {
      @Override
      public BeeRowSet processError(SQLException ex) {
        LogUtils.error(logger, ex);
        return null;
      }

      @Override
      public BeeRowSet processResultSet(ResultSet rs) throws SQLException {
        return rsToBeeRowSet(rs, view);
      }

      @Override
      public BeeRowSet processUpdateCount(int updateCount) {
        LogUtils.warning(logger, "Query must return a ResultSet");
        return null;
      }
    });
  }

  public long insertData(SqlInsert si) {
    Assert.notNull(si);

    String source = (String) si.getTarget().getSource();
    boolean requiresId = BeeUtils.isEmpty(si.getDataSource()) && sys.isTable(source);
    long id = 0;

    Assert.state(requiresId || !si.isEmpty());

    if (requiresId) {
      String lockFld = sys.getLockName(source);

      if (!si.hasField(lockFld)) {
        si.addConstant(lockFld, System.currentTimeMillis());
      }
      String idFld = sys.getIdName(source);

      if (!si.hasField(idFld)) {
        id = ig.getId(source);
        si.addConstant(idFld, id);
      }
    }
    if (BeeUtils.isEmpty(updateData(si))) {
      id = -1;
    }
    return id;
  }

  public boolean isDbTable(String dbName, String dbSchema, String table) {
    Assert.notEmpty(table);
    return !BeeUtils.isEmpty(dbTables(dbName, dbSchema, table));
  }

  public BeeRowSet rsToBeeRowSet(ResultSet rs, BeeView view) throws SQLException {
    BeeColumn[] rsCols = JdbcUtils.getColumns(rs);
    int cols = BeeUtils.isEmpty(view) ? rsCols.length : view.getColumnCount();
    BeeColumn[] columns = new BeeColumn[cols];

    int idIndex = -1;
    int verIndex = -1;

    int j = 0;
    for (BeeColumn col : rsCols) {
      if (!BeeUtils.isEmpty(view)) {
        String colName = col.getId();

        if (view.hasColumn(colName)) {
          String fld = view.getField(colName);

          switch (sys.getTableField(view.getTable(colName), fld).getType()) {
            case BOOLEAN:
              col.setType(ValueType.BOOLEAN);
              break;
            case DATE:
              col.setType(ValueType.DATE);
              break;
            case DATETIME:
              col.setType(ValueType.DATETIME);
              break;
            default:
          }
        } else {
          if (idIndex < 0) {
            idIndex = col.getIndex();
          } else {
            verIndex = col.getIndex();
          }
          continue;
        }
      }
      columns[j++] = col;
    }
    BeeRowSet result = new BeeRowSet(columns);
    long idx = 0;

    while (rs.next()) {
      String[] row = new String[cols];

      for (int i = 0; i < cols; i++) {
        switch (columns[i].getType()) {
          case BOOLEAN:
            row[i] = BooleanValue.pack(rs.getBoolean(columns[i].getIndex()));
            break;
          case NUMBER:
            if (columns[i].getScale() > 0) {
              row[i] = BeeUtils.removeTrailingZeros(rs.getString(columns[i].getIndex()));
              break;
            }
            //$FALL-THROUGH$
          default:
            row[i] = rs.getString(columns[i].getIndex());
        }
      }
      if (idIndex < 0) {
        idx++;
      } else {
        idx = rs.getLong(idIndex);
      }
      if (verIndex < 0) {
        result.addRow(idx, row);
      } else {
        result.addRow(idx, rs.getLong(verIndex), row);
      }
    }
    if (idIndex >= 0) {
      result.setViewName(view.getName());
    }
    LogUtils.info(logger, "cols:", result.getNumberOfColumns(), "rows:", result.getNumberOfRows());
    return result;
  }

  public SimpleRowSet rsToSimpleRowSet(ResultSet rs) throws SQLException {
    BeeColumn[] rsCols = JdbcUtils.getColumns(rs);
    int cols = rsCols.length;
    String[] columns = new String[cols];

    for (int i = 0; i < cols; i++) {
      columns[i] = rsCols[i].getId();
    }
    SimpleRowSet res = new SimpleRowSet(columns);

    while (rs.next()) {
      String[] row = new String[cols];

      for (int i = 0; i < cols; i++) {
        row[i] = rs.getString(rsCols[i].getIndex());
      }
      res.addRow(row);
    }
    LogUtils.info(logger, "Retrieved rows:", res.getNumberOfRows());
    return res;
  }

  public int updateData(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    activateTables(query);

    Integer res = processSql(query.getQuery(), new SqlHandler<Integer>() {
      @Override
      public Integer processError(SQLException ex) {
        LogUtils.error(logger, ex);
        return -1;
      }

      @Override
      public Integer processResultSet(ResultSet rs) throws SQLException {
        LogUtils.warning(logger, "Data modification query must not return a ResultSet");
        return -1;
      }

      @Override
      public Integer processUpdateCount(int updateCount) {
        LogUtils.info(logger, "Affected rows:", updateCount);
        return updateCount;
      }
    });

    if (res == null) {
      res = 0;
    }
    return res;
  }

  private void activateTables(IsQuery query) {
    Collection<String> sources = query.getSources();

    if (!BeeUtils.isEmpty(sources)) {
      for (String source : query.getSources()) {
        if (sys.isTable(source)) {
          sys.activateTable(source);
        }
      }
    }
  }

  private SimpleRowSet getSingleColumn(IsQuery query) {
    SimpleRowSet res = getData(query);
    Assert.notNull(res);
    Assert.isTrue(res.getNumberOfColumns() == 1, "Result must contain exactly one column");
    return res;
  }

  private SimpleRowSet getSingleRow(IsQuery query) {
    SimpleRowSet res = getData(query);
    Assert.notNull(res);
    Assert.isTrue(res.getNumberOfRows() <= 1, "Result must contain exactly one row");
    return res;
  }

  private SimpleRowSet getSingleValue(IsQuery query) {
    SimpleRowSet res = getData(query);
    Assert.notNull(res);
    Assert.isTrue(res.getNumberOfColumns() == 1, "Result must contain exactly one column");
    Assert.isTrue(res.getNumberOfRows() == 1, "Result must contain exactly one row");
    return res;
  }

  private <T> T processSql(String sql, SqlHandler<T> callback) {
    Assert.notEmpty(sql);
    Assert.notEmpty(callback);

    DataSource ds = dsb.locateDs(SqlBuilderFactory.getEngine()).getDs();

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    T result = null;

    LogUtils.info(logger, "SQL:", sql);

    try {
      con = ds.getConnection();
      stmt = con.createStatement();

      boolean isResultSet = stmt.execute(sql);

      if (isResultSet) {
        rs = stmt.getResultSet();
        result = callback.processResultSet(rs);
      } else {
        result = callback.processUpdateCount(stmt.getUpdateCount());
      }

    } catch (SQLException ex) {
      result = callback.processError(ex);
    } finally {
      JdbcUtils.closeResultSet(rs);
      JdbcUtils.closeStatement(stmt);
      JdbcUtils.closeConnection(con);
    }
    return result;
  }
}
