package com.butent.bee.server.data;

import com.google.common.collect.Lists;

import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlConstants;
import com.butent.bee.server.sql.SqlConstants.SqlKeyword;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.BeeDataSource;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst.SqlEngine;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * Manages SQL related requests from client side.
 */

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class QueryServiceBean {

  /**
   * Is a private interface for SQL processing.
   */

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

  public SqlEngine dbEngine(String dsn) {
    SqlEngine sqlEngine = null;

    if (!BeeUtils.isEmpty(dsn)) {
      BeeDataSource bds = dsb.locateDs(dsn);

      if (bds != null) {
        String engine;
        Connection con = null;
        try {
          con = bds.getDs().getConnection();
          engine = con.getMetaData().getDatabaseProductName();

        } catch (SQLException e) {
          LogUtils.error(logger, e);
          engine = null;
        } finally {
          JdbcUtils.closeConnection(con);
        }
        sqlEngine = SqlEngine.detectEngine(engine);

        if (sqlEngine == null) {
          LogUtils.severe(logger, "DSN:", dsn, "Unknown SQL engine:", engine);
        }
      }
    }
    return sqlEngine;
  }

  public boolean dbExists(String source, IsCondition where) {
    return dbRowCount(new SqlSelect()
        .addConstant(null, "dummy").addFrom(source).setWhere(where)) > 0;
  }

  public SimpleRowSet dbFields(String dbName, String dbSchema, String table) {
    return getData(SqlUtils.dbFields(dbName, dbSchema, table));
  }

  public SimpleRowSet dbForeignKeys(String dbName, String dbSchema, String table,
      String refTable) {
    return getData(SqlUtils.dbForeignKeys(dbName, dbSchema, table, refTable));
  }

  public SimpleRowSet dbIndexes(String dbName, String dbSchema, String table) {
    return getData(SqlUtils.dbIndexes(dbName, dbSchema, table));
  }

  public SimpleRowSet dbKeys(String dbName, String dbSchema, String table, SqlKeyword... types) {
    return getData(SqlUtils.dbKeys(dbName, dbSchema, table, types));
  }

  public String dbName() {
    IsQuery query = SqlUtils.dbName();

    if (!BeeUtils.isEmpty(query.getQuery())) {
      return getValue(query);
    }
    return "";
  }

  public int dbRowCount(String source, IsCondition where) {
    return dbRowCount(new SqlSelect().addConstant(null, "dummy").addFrom(source).setWhere(where));
  }

  public int dbRowCount(SqlSelect ss) {
    SimpleRowSet res;

    if (BeeUtils.allEmpty(ss.getGroupBy(), ss.getUnion())) {
      res = getData(ss.copyOf().resetFields().resetOrder().addCount("cnt"));
    } else {
      res = getData(new SqlSelect().addCount("cnt").addFrom(ss.resetOrder(), "als"));
    }
    if (res == null) {
      return -1;
    }
    return BeeUtils.unbox(res.getInt(0, 0));
  }

  public String dbSchema() {
    IsQuery query = SqlUtils.dbSchema();

    if (!BeeUtils.isEmpty(query.getQuery())) {
      return getValue(query);
    }
    return "";
  }

  public String[] dbTables(String dbName, String dbSchema, String table) {
    return getData(SqlUtils.dbTables(dbName, dbSchema, table)).getColumn(SqlConstants.TBL_NAME);
  }

  public SimpleRowSet dbTriggers(String dbName, String dbSchema, String table) {
    return getData(SqlUtils.dbTriggers(dbName, dbSchema, table));
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

  public Boolean[] getBooleanColumn(IsQuery query) {
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

  public Double getDouble(IsQuery query) {
    return getSingleValue(query).getDouble(0, 0);
  }

  public Double[] getDoubleColumn(IsQuery query) {
    return getSingleColumn(query).getDoubleColumn(0);
  }

  public Integer getInt(IsQuery query) {
    return getSingleValue(query).getInt(0, 0);
  }

  public Integer[] getIntColumn(IsQuery query) {
    return getSingleColumn(query).getIntColumn(0);
  }

  public Long getLong(IsQuery query) {
    return getSingleValue(query).getLong(0, 0);
  }

  public Long[] getLongColumn(IsQuery query) {
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

  public List<BeeColumn> getViewColumns(final BeeView view) {
    Assert.notNull(view);

    SqlSelect ss = view.getQuery().setWhere(SqlUtils.sqlFalse()).resetOrder();
    activateTables(ss);

    return processSql(ss.getQuery(), new SqlHandler<List<BeeColumn>>() {
      @Override
      public List<BeeColumn> processError(SQLException ex) {
        LogUtils.error(logger, ex);
        return null;
      }

      @Override
      public List<BeeColumn> processResultSet(ResultSet rs) throws SQLException {
        BeeColumn[] rsCols = JdbcUtils.getColumns(rs);
        List<BeeColumn> columns = Lists.newArrayList();

        for (BeeColumn col : rsCols) {
          String colName = col.getId();
          if (view.hasColumn(colName)) {
            initColumn(view, colName, col);
            columns.add(col);
          }
        }
        return columns;
      }

      @Override
      public List<BeeColumn> processUpdateCount(int updateCount) {
        LogUtils.warning(logger, "Query must return a ResultSet");
        return null;
      }
    });
  }

  public BeeRowSet getViewData(IsQuery query, final BeeView view) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());
    activateTables(query);

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
    return insertDataWithResponse(si).getResponse(-1L, logger);
  }

  public ResponseObject insertDataWithResponse(SqlInsert si) {
    Assert.notNull(si);

    String source = (String) si.getTarget().getSource();
    boolean requiresId = BeeUtils.isEmpty(si.getDataSource()) && sys.isTable(source);
    long id = 0;

    Assert.state(requiresId || !si.isEmpty());

    if (requiresId) {
      String versionFld = sys.getVersionName(source);

      if (!si.hasField(versionFld)) {
        si.addConstant(versionFld, System.currentTimeMillis());
      }
      String idFld = sys.getIdName(source);

      if (si.hasField(idFld)) {
        id = ((Value) si.getValue(idFld).getValue()).getLong();
      } else {
        id = ig.getId(source);
        si.addConstant(idFld, id);
      }
    }
    ResponseObject response = updateDataWithResponse(si);

    if (!response.hasErrors()) {
      response.setResponse(id);
    }
    return response;
  }

  public boolean isDbTable(String dbName, String dbSchema, String table) {
    Assert.notEmpty(table);
    return !BeeUtils.isEmpty(dbTables(dbName, dbSchema, table));
  }

  public int updateData(IsQuery query) {
    return updateDataWithResponse(query).getResponse(-1, logger);
  }

  public ResponseObject updateDataWithResponse(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    activateTables(query);

    ResponseObject res = processSql(query.getQuery(), new SqlHandler<ResponseObject>() {
      @Override
      public ResponseObject processError(SQLException ex) {
        return ResponseObject.error(ex).setResponse(-1);
      }

      @Override
      public ResponseObject processResultSet(ResultSet rs) throws SQLException {
        return ResponseObject.error("Data modification query must not return a ResultSet")
            .setResponse(-1);
      }

      @Override
      public ResponseObject processUpdateCount(int updateCount) {
        return ResponseObject.response(updateCount);
      }
    });

    if (res == null) {
      res = ResponseObject.error("System error. Check server log for more details").setResponse(-1);
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

  private void initColumn(BeeView view, String colName, BeeColumn column) {
    column.setType(view.getColumnType(colName).toValueType());

    if (view.isColReadOnly(colName)) {
      column.setReadOnly(true);
    }
    column.setLevel(view.getColumnLevel(colName));
  }
  
  private <T> T processSql(String sql, SqlHandler<T> callback) {
    Assert.notEmpty(sql);
    Assert.notEmpty(callback);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    T result = null;

    LogUtils.infoSplit(logger, "SQL:", sql);

    try {
      String dsn = SqlBuilderFactory.getDsn();
      BeeDataSource bds = dsb.locateDs(dsn);

      if (bds == null) {
        throw new SQLException("Data source name [" + dsn + "] not found");
      }
      con = bds.getDs().getConnection();
      stmt = con.createStatement();

      long start = System.nanoTime();
      boolean isResultSet = stmt.execute(sql);
      LogUtils.infoNow(logger, String.format("[%.6f]", (System.nanoTime() - start) / 1e9));

      if (isResultSet) {
        rs = stmt.getResultSet();
        result = callback.processResultSet(rs);
      } else {
        int cnt = stmt.getUpdateCount();
        result = callback.processUpdateCount(cnt < 0 ? 0 : cnt);
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

  private BeeRowSet rsToBeeRowSet(ResultSet rs, BeeView view) throws SQLException {
    BeeColumn[] rsCols = JdbcUtils.getColumns(rs);
    List<BeeColumn> columns = Lists.newArrayList();
    int idIndex = -1;
    int versionIndex = -1;

    for (BeeColumn col : rsCols) {
      if (!BeeUtils.isEmpty(view)) {
        String colName = col.getId();

        if (view.hasColumn(colName)) {
          initColumn(view, colName, col);

        } else if (BeeUtils.same(colName, view.getSourceIdName())) {
          idIndex = col.getIndex();
          continue;
        
        } else if (BeeUtils.same(colName, view.getSourceVersionName())) {
          versionIndex = col.getIndex();
          continue;
        }
      }
      columns.add(col);
    }
    int cols = columns.size();
    BeeRowSet result = new BeeRowSet(columns);
    long idx = 0;

    while (rs.next()) {
      String[] row = new String[cols];

      for (int i = 0; i < cols; i++) {
        BeeColumn column = columns.get(i);
        int colIndex = column.getIndex();

        switch (column.getType()) {
          case BOOLEAN:
            row[i] = BooleanValue.pack(rs.getBoolean(colIndex));
            if (rs.wasNull()) {
              row[i] = null;
            }
            break;
          case NUMBER:
          case DECIMAL:
            if (column.getScale() > 0) {
              row[i] = BeeUtils.removeTrailingZeros(rs.getString(colIndex));
              break;
            }
            //$FALL-THROUGH$
          default:
            row[i] = rs.getString(colIndex);
        }
      }
      if (idIndex < 0) {
        idx++;
      } else {
        idx = rs.getLong(idIndex);
      }
      if (versionIndex < 0) {
        result.addRow(idx, row);
      } else {
        result.addRow(idx, rs.getLong(versionIndex), row);
      }
    }
    if (idIndex >= 0) {
      result.setViewName(view.getName());
    }
    LogUtils.info(logger, "cols:", result.getNumberOfColumns(), "rows:", result.getNumberOfRows());
    return result;
  }

  private SimpleRowSet rsToSimpleRowSet(ResultSet rs) throws SQLException {
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
}
