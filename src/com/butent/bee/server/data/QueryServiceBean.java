package com.butent.bee.server.data;

import com.google.common.collect.Lists;

import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.BeeDataSource;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeConst.SqlEngine;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SqlConstants.SqlKeyword;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

  private abstract class SqlHandler<T> {

    public T processError(SQLException ex) {
      String error = null;

      Map<String, String> params = prm.getMap(CommonsConstants.COMMONS_MODULE,
          BeeUtils.join(BeeConst.STRING_EMPTY, CommonsConstants.PRM_SQL_MESSAGES,
              SqlBuilderFactory.getBuilder().getEngine()));

      if (!BeeUtils.isEmpty(params)) {
        String msg = ex.getMessage();

        for (String key : params.keySet()) {
          if (msg.matches("(?s)" + key)) {
            error = msg.replaceAll("(?s)" + key, params.get(key));
            break;
          }
        }
      }
      if (error != null) {
        throw new BeeRuntimeException(error);
      } else {
        throw new BeeRuntimeException(ex);
      }
    }

    public abstract T processResultSet(ResultSet rs) throws SQLException;

    public abstract T processUpdateCount(int updateCount);
  }

  private static BeeLogger logger = LogUtils.getLogger(QueryServiceBean.class);

  @EJB
  DataSourceBean dsb;
  @EJB
  IdGeneratorBean ig;
  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  ParamHolderBean prm;

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
          logger.error(e);
          engine = null;
        } finally {
          JdbcUtils.closeConnection(con);
        }
        sqlEngine = SqlEngine.detectEngine(engine);

        if (sqlEngine == null) {
          logger.severe("DSN:", dsn, "Unknown SQL engine:", engine);
        }
      }
    }
    return sqlEngine;
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

  public String dbSchema() {
    IsQuery query = SqlUtils.dbSchema();

    if (!BeeUtils.isEmpty(query.getQuery())) {
      return getValue(query);
    }
    return "";
  }

  public boolean dbSchemaExists(String dbName, String schema) {
    Assert.notEmpty(schema);
    return !ArrayUtils.isEmpty(dbSchemas(dbName, schema));
  }

  public String[] dbSchemas(String dbName, String schema) {
    return getColumn(SqlUtils.dbSchemas(dbName, schema));
  }

  public boolean dbTableExists(String dbName, String dbSchema, String table) {
    Assert.notEmpty(table);
    return dbTables(dbName, dbSchema, table).getNumberOfRows() > 0;
  }

  public SimpleRowSet dbTables(String dbName, String dbSchema, String table) {
    return getData(SqlUtils.dbTables(dbName, dbSchema, table));
  }

  public SimpleRowSet dbTriggers(String dbName, String dbSchema, String table) {
    return getData(SqlUtils.dbTriggers(dbName, dbSchema, table));
  }

  public Object doSql(String sql) {
    Assert.notEmpty(sql);

    return processSql(sql, new SqlHandler<Object>() {
      @Override
      public Object processError(SQLException ex) {
        logger.error(ex);
        return ex.getMessage();
      }

      @Override
      public Object processResultSet(ResultSet rs) throws SQLException {
        return rsToBeeRowSet(rs, null);
      }

      @Override
      public Object processUpdateCount(int updateCount) {
        logger.debug("Affected rows:", updateCount);
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
      public SimpleRowSet processResultSet(ResultSet rs) throws SQLException {
        return rsToSimpleRowSet(rs);
      }

      @Override
      public SimpleRowSet processUpdateCount(int updateCount) {
        throw new BeeRuntimeException("Query must return a ResultSet");
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
    return getSingleRow(query).getRow(0);
  }

  public List<SearchResult> getSearchResults(String viewName, Filter filter) {
    List<SearchResult> results = Lists.newArrayList();

    BeeRowSet rowSet = getViewData(viewName, filter);
    if (rowSet != null) {
      for (BeeRow row : rowSet.getRows()) {
        results.add(new SearchResult(viewName, row));
      }
    }
    return results;
  }

  public String getValue(IsQuery query) {
    return getSingleValue(query).getValue(0, 0);
  }

  public String[] getValues(IsQuery query) {
    return getSingleRow(query).getValues(0);
  }

  public BeeRowSet getViewData(String viewName) {
    return getViewData(viewName, null, null);
  }

  public BeeRowSet getViewData(String viewName, Filter filter) {
    return getViewData(viewName, filter, null);
  }

  public BeeRowSet getViewData(String viewName, Filter filter, Order order) {
    return getViewData(viewName, filter, order, BeeConst.UNDEF, BeeConst.UNDEF, null);
  }

  public BeeRowSet getViewData(String viewName, Filter filter, Order order, int limit, int offset,
      List<String> columns) {

    BeeView view = sys.getView(viewName);
    SqlSelect ss = view.getQuery(filter, order, columns);

    if (limit > 0) {
      ss.setLimit(limit);
    }
    if (offset > 0) {
      ss.setOffset(offset);
    }
    return getViewData(ss, view);
  }

  public BeeRowSet getViewData(final SqlSelect query, final BeeView view) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());
    activateTables(query);

    return processSql(query.getQuery(), new SqlHandler<BeeRowSet>() {
      @Override
      public BeeRowSet processResultSet(ResultSet rs) throws SQLException {
        BeeRowSet rowset = rsToBeeRowSet(rs, view);
        sys.postViewEvent(new ViewQueryEvent(view.getName(), query, rowset));
        return rowset;
      }

      @Override
      public BeeRowSet processUpdateCount(int updateCount) {
        throw new BeeRuntimeException("Query must return a ResultSet");
      }
    });
  }

  public int getViewSize(String viewName, Filter filter) {
    return sqlCount(sys.getView(viewName).getQuery(filter));
  }

  public long insertData(SqlInsert si) {
    return insertDataWithResponse(si).getResponse(-1L, logger);
  }

  public ResponseObject insertDataWithResponse(SqlInsert si) {
    Assert.notNull(si);

    String target = si.getTarget();
    boolean requiresId = (si.getDataSource() == null) && sys.isTable(target);
    long id = 0;

    Assert.state(requiresId || !si.isEmpty());

    if (requiresId) {
      String versionFld = sys.getVersionName(target);

      if (!si.hasField(versionFld)) {
        si.addConstant(versionFld, System.currentTimeMillis());
      }
      String idFld = sys.getIdName(target);

      if (si.hasField(idFld)) {
        id = ((Value) si.getValue(idFld).getValue()).getLong();
      } else {
        id = ig.getId(target);
        si.addConstant(idFld, id);
      }
    }
    ResponseObject response = updateDataWithResponse(si);

    if (!response.hasErrors()) {
      response.setResponse(id);
    }
    return response;
  }

  public int sqlCount(String source, IsCondition where) {
    return sqlCount(new SqlSelect().addConstant(null, "dummy").addFrom(source).setWhere(where));
  }

  public int sqlCount(SqlSelect query) {
    SimpleRowSet res;
    SqlSelect ss = query.copyOf().resetOrder();

    if (BeeUtils.isEmpty(ss.getGroupBy()) && BeeUtils.isEmpty(ss.getUnion())) {
      res = getData(ss.resetFields().addCount("cnt"));
    } else {
      res = getData(new SqlSelect().addCount("cnt").addFrom(ss, "als"));
    }
    if (res == null) {
      return BeeConst.UNDEF;
    }
    return BeeUtils.unbox(res.getInt(0, 0));
  }

  public String sqlCreateTemp(SqlSelect query) {
    String tmp = SqlUtils.temporaryName();
    updateData(new SqlCreate(tmp).setDataSource(query));
    return tmp;
  }

  public void sqlDropTemp(String tmp) {
    Assert.state(!sys.isTable(tmp), "Can't drop a base table: " + tmp);
    updateData(SqlUtils.dropTable(tmp));
  }

  public boolean sqlExists(String source, IsCondition where) {
    return sqlCount(new SqlSelect()
        .addConstant(null, "dummy").addFrom(source).setWhere(where)) > 0;
  }

  public void sqlIndex(String tmp, String... fields) {
    Assert.state(!sys.isTable(tmp), "Can't index a base table: " + tmp);
    updateData(SqlUtils.createIndex(false, tmp, SqlUtils.uniqueName(), fields));
  }

  public String sqlValue(String source, String field, long id) {
    return getValue(new SqlSelect()
        .addFields(source, field)
        .addFrom(source)
        .setWhere(SqlUtils.equal(source, sys.getIdName(source), id)));
  }

  public int updateData(IsQuery query) {
    return updateDataWithResponse(query).getResponse(-1, logger);
  }

  public ResponseObject updateDataWithResponse(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    doSql(SqlUtils.setSqlParameter(SystemBean.AUDIT_USER, usr.getCurrentUserId()).getQuery());

    activateTables(query);

    ResponseObject res = processSql(query.getQuery(), new SqlHandler<ResponseObject>() {
      @Override
      public ResponseObject processResultSet(ResultSet rs) throws SQLException {
        throw new BeeRuntimeException("Data modification query must not return a ResultSet");
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
      for (String source : sources) {
        if (sys.isTable(source) && !sys.getTable(source).isActive()) {
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
    Assert.isTrue(res.getNumberOfRows() <= 1, "Result must contain zero or one row");
    return res;
  }

  private SimpleRowSet getSingleValue(IsQuery query) {
    SimpleRowSet res = getData(query);
    Assert.notNull(res);
    Assert.isTrue(res.getNumberOfColumns() == 1, "Result must contain exactly one column");
    Assert.isTrue(res.getNumberOfRows() <= 1, "Result must contain zero or one row");
    return res;
  }

  private <T> T processSql(String sql, SqlHandler<T> callback) {
    Assert.notEmpty(sql);
    Assert.notNull(callback);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    T result = null;

    logger.debug("SQL:", sql);

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
      logger.debug(String.format("[%.6f]", (System.nanoTime() - start) / 1e9));

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
      if (view != null) {
        String colName = col.getId();

        if (view.hasColumn(colName)) {
          view.initColumn(colName, col);

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
          case DATE:
            Long time = BeeUtils.toLongOrNull(rs.getString(colIndex));
            row[i] = (time == null) ? null : BeeUtils.toString(time / TimeUtils.MILLIS_PER_DAY);
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
    logger.debug("cols:", result.getNumberOfColumns(), "rows:", result.getNumberOfRows());
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
    logger.debug("Retrieved rows:", res.getNumberOfRows());
    return res;
  }
}
