package com.butent.bee.server.data;

import com.google.common.collect.Lists;

import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.data.DataEvent.TableModifyEvent;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.HasTarget;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
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
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.SqlConstants.SqlKeyword;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
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
import javax.sql.DataSource;

/**
 * Manages SQL related requests from client side.
 */

@Stateless
@LocalBean
public class QueryServiceBean {

  /**
   * Is a private interface for SQL processing.
   */

  public interface ResultSetProcessor<T> {
    T processResultSet(ResultSet rs) throws SQLException;
  }

  private abstract class SqlHandler<T> implements ResultSetProcessor<T> {

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

    public abstract T processUpdateCount(int updateCount);
  }

  private static final String EDITABLE_STATE_COLUMN = RightsState.EDITABLE.name();

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

  public SimpleRowSet dbConstraints(String dbName, String dbSchema, String table,
      SqlKeyword... types) {
    return getData(SqlUtils.dbConstraints(dbName, dbSchema, table, types));
  }

  public SqlEngine dbEngine(String dsn) {
    SqlEngine sqlEngine = null;

    if (!BeeUtils.isEmpty(dsn)) {
      BeeDataSource bds = dsb.locateDs(dsn);

      if (bds != null) {
        sqlEngine = dbEngine(bds.getDs());
      }
    }
    return sqlEngine;
  }

  public SqlEngine dbEngine(DataSource ds) {
    SqlEngine sqlEngine = null;

    if (ds != null) {
      String engine;
      Connection con = null;
      try {
        con = ds.getConnection();
        engine = con.getMetaData().getDatabaseProductName();

      } catch (SQLException e) {
        logger.error(e);
        engine = null;
      } finally {
        JdbcUtils.closeConnection(con);
      }
      sqlEngine = SqlEngine.detectEngine(engine);
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

  @TransactionAttribute(TransactionAttributeType.MANDATORY)
  public Object doSql(String sql) {
    BeeDataSource bds = dsb.locateDs(SqlBuilderFactory.getDsn());
    Assert.notNull(bds);

    return doSql(bds.getDs(), sql);
  }

  public Object doSql(DataSource ds, String sql) {
    Assert.notNull(ds);
    Assert.notEmpty(sql);

    return processSql(ds, sql, new SqlHandler<Object>() {
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
        logger.debug("affected rows:", updateCount);
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
    return getData(null, query, new ResultSetProcessor<SimpleRowSet>() {
      @Override
      public SimpleRowSet processResultSet(ResultSet rs) throws SQLException {
        return rsToSimpleRowSet(rs);
      }
    });
  }

  public <T> T getData(DataSource ds, IsQuery query, final ResultSetProcessor<T> callback) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    String sql;

    if (ds == null) {
      activateTables(query);
      sql = query.getQuery();
    } else {
      sql = query.getSqlString(SqlBuilderFactory.getBuilder(dbEngine(ds)));
    }
    return processSql(ds, sql, new SqlHandler<T>() {
      @Override
      public T processResultSet(ResultSet rs) throws SQLException {
        return callback.processResultSet(rs);
      }

      @Override
      public T processUpdateCount(int updateCount) {
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

  public SimpleRowSet getHistogram(String viewName, Filter filter, List<String> columns,
      List<String> order) {

    BeeView view = sys.getView(viewName);
    SqlSelect viewQuery = view.getQuery(filter, null, columns, sys.getViewFinder());

    String queryAlias = "Hist_" + SqlUtils.uniqueName();
    String countAlias = "Count_" + SqlUtils.uniqueName();

    SqlSelect ss = new SqlSelect();
    for (String colName : columns) {
      ss.addFields(queryAlias, colName);
      ss.addGroup(queryAlias, colName);
    }

    ss.addCount(countAlias).addFrom(viewQuery, queryAlias);

    if (!BeeUtils.isEmpty(order)) {
      for (String colName : order) {
        ss.addOrder(queryAlias, colName);
      }
    }

    return getData(ss);
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

  public String getNextNumber(String tblName, String fldName, String prefix, String prefixFld) {
    Object value = null;

    if (!BeeUtils.allEmpty(tblName, fldName)) {
      IsCondition clause = null;
      IsExpression xpr = null;

      if (BeeUtils.isEmpty(prefix)) {
        xpr = SqlUtils.field(tblName, fldName);
      } else {
        if (!BeeUtils.isEmpty(prefixFld)) {
          xpr = SqlUtils.field(tblName, fldName);
          clause = SqlUtils.equals(tblName, prefixFld, prefix);
        } else {
          xpr = SqlUtils.substring(tblName, fldName, prefix.length() + 1);
          clause = SqlUtils.startsWith(tblName, fldName, prefix);
        }
      }
      clause = SqlUtils.and(clause,
          SqlUtils.compare(SqlUtils.length(xpr), Operator.EQ,
              new SqlSelect()
                  .addMax(SqlUtils.length(xpr), "length")
                  .addFrom(tblName)
                  .setWhere(clause)));

      String maxValue = getValue(new SqlSelect()
          .addMax(xpr, "value")
          .addFrom(tblName)
          .setWhere(clause));

      value = BeeUtils.nextString(maxValue);
    }
    return BeeUtils.join(BeeConst.STRING_EMPTY, BeeUtils.isEmpty(prefixFld) ? prefix : null, value);
  }

  public Long[] getRelatedValues(String tableName, String filterColumn, long filterValue,
      String resultColumn) {

    SqlSelect query = new SqlSelect()
        .addFields(tableName, resultColumn)
        .addFrom(tableName)
        .addOrder(tableName, sys.getIdName(tableName));

    boolean selfRelationsMode = BeeUtils.same(filterColumn, resultColumn)
        && BeeUtils.same(tableName, CommonsConstants.TBL_RELATIONS);

    if (selfRelationsMode) {
      String als = SqlUtils.uniqueName();

      query.addFromInner(tableName, als,
          SqlUtils.and(sys.joinTables(tableName, als, CommonsConstants.COL_RELATION),
              SqlUtils.notNull(tableName, resultColumn),
              SqlUtils.equals(als, filterColumn, filterValue)));
    } else {
      query.setWhere(SqlUtils.and(SqlUtils.equals(tableName, filterColumn, filterValue),
          SqlUtils.notNull(tableName, resultColumn)));
    }
    return getLongColumn(query);
  }

  public SimpleRow getRow(IsQuery query) {
    SimpleRowSet res = getData(query);
    Assert.notNull(res);
    Assert.isTrue(res.getNumberOfRows() <= 1, "Result must contain zero or one row");
    return res.getRow(0);
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

  public BeeRowSet getViewData(String viewName) {
    return getViewData(viewName, null);
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
    SqlSelect ss = view.getQuery(filter, order, columns, sys.getViewFinder());

    if (limit > 0) {
      ss.setLimit(limit);
    }
    if (offset > 0) {
      ss.setOffset(offset);
    }
    return getViewData(ss, view);
  }

  public int getViewSize(String viewName, Filter filter) {
    return sqlCount(sys.getView(viewName).getQuery(filter, sys.getViewFinder()));
  }

  @TransactionAttribute(TransactionAttributeType.MANDATORY)
  public long insertData(SqlInsert si) {
    return insertDataWithResponse(si).getResponse(-1L, logger);
  }

  @TransactionAttribute(TransactionAttributeType.MANDATORY)
  public ResponseObject insertDataWithResponse(SqlInsert si) {
    Assert.notNull(si);

    String target = si.getTarget();
    boolean requiresId = !si.isMultipleInsert() && sys.isTable(target);
    long id = 0;

    Assert.state(requiresId || !si.isEmpty());

    if (requiresId) {
      String versionFld = sys.getVersionName(target);

      if (!si.hasField(versionFld)) {
        si.addConstant(versionFld, System.currentTimeMillis());
      }
      String idFld = sys.getIdName(target);

      if (si.hasField(idFld)) {
        Object value = si.getValue(idFld).getValue();

        if (value instanceof Value) {
          id = ((Value) value).getLong();
        }
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

  public int sqlCount(String source, IsCondition where) {
    return sqlCount(new SqlSelect().addConstant(null, "dummy").addFrom(source).setWhere(where));
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
    updateData(SqlUtils.createIndex(tmp, SqlUtils.uniqueName(), Lists.newArrayList(fields), false));
  }

  public String sqlValue(String source, String field, long id) {
    return getValue(new SqlSelect()
        .addFields(source, field)
        .addFrom(source)
        .setWhere(sys.idEquals(source, id)));
  }

  @TransactionAttribute(TransactionAttributeType.MANDATORY)
  public int updateData(IsQuery query) {
    return updateDataWithResponse(query).getResponse(-1, logger);
  }

  @TransactionAttribute(TransactionAttributeType.MANDATORY)
  public ResponseObject updateDataWithResponse(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    doSql(SqlUtils.setSqlParameter(SystemBean.AUDIT_USER, usr.getCurrentUserId()).getQuery());

    activateTables(query);

    final TableModifyEvent event;

    if (query instanceof HasTarget) {
      event = new TableModifyEvent(((HasTarget) query).getTarget(), query);
      sys.postDataEvent(event);

      if (event.hasErrors()) {
        ResponseObject response = new ResponseObject();

        for (String error : event.getErrorMessages()) {
          response.addError(error);
        }
        return response;
      }
    } else {
      event = null;
    }
    ResponseObject res = processSql(null, query.getQuery(), new SqlHandler<ResponseObject>() {
      @Override
      public ResponseObject processResultSet(ResultSet rs) throws SQLException {
        throw new BeeRuntimeException("Data modification query must not return a ResultSet");
      }

      @Override
      public ResponseObject processUpdateCount(int updateCount) {
        if (event != null) {
          event.setUpdateCount(updateCount);
          sys.postDataEvent(event);
        }
        logger.debug("affected rows:", updateCount);
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

  private SimpleRowSet getSingleValue(IsQuery query) {
    SimpleRowSet res = getData(query);
    Assert.notNull(res);
    Assert.isTrue(res.getNumberOfColumns() == 1, "Result must contain exactly one column");
    Assert.isTrue(res.getNumberOfRows() <= 1, "Result must contain zero or one row");
    return res;
  }

  private BeeRowSet getViewData(final SqlSelect query, final BeeView view) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    String tableName = view.getSourceName();
    String tableAlias = view.getSourceAlias();

    sys.filterVisibleState(query, tableName, tableAlias);

    BeeTable table = sys.getTable(tableName);
    String stateAlias = table.joinState(query, tableAlias, RightsState.EDITABLE);

    if (!BeeUtils.isEmpty(stateAlias)) {
      query.addExpr(SqlUtils.sqlIf(table.checkState(stateAlias, RightsState.EDITABLE,
          table.areRecordsEditable(), usr.getUserRoles(usr.getCurrentUserId())), 1, 0),
          EDITABLE_STATE_COLUMN);
    }

    activateTables(query);

    final ViewQueryEvent event = new ViewQueryEvent(view.getName(), query);
    sys.postDataEvent(event);

    return processSql(null, query.getQuery(), new SqlHandler<BeeRowSet>() {
      @Override
      public BeeRowSet processResultSet(ResultSet rs) throws SQLException {
        event.setRowset(rsToBeeRowSet(rs, view));
        sys.postDataEvent(event);
        return event.getRowset();
      }

      @Override
      public BeeRowSet processUpdateCount(int updateCount) {
        throw new BeeRuntimeException("Query must return a ResultSet");
      }
    });
  }

  private <T> T processSql(DataSource ds, String sql, SqlHandler<T> callback) {
    Assert.notEmpty(sql);
    Assert.notNull(callback);

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    T result = null;
    DataSource dataSource = ds;

    if (dataSource == null) {
      String dsn = SqlBuilderFactory.getDsn();
      BeeDataSource bds = dsb.locateDs(dsn);

      if (bds == null) {
        result = callback.processError(new SQLException("Data source [" + dsn + "] not found"));
        return result;
      }
      dataSource = bds.getDs();
    }
    logger.debug("SQL:", sql);

    try {
      con = dataSource.getConnection();
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
    List<BeeColumn> rsCols = JdbcUtils.getColumns(rs);

    int idIndex = BeeConst.UNDEF;
    int versionIndex = BeeConst.UNDEF;
    int editableIndex = BeeConst.UNDEF;

    BeeRowSet result;
    List<Integer> indexes = Lists.newArrayListWithCapacity(rsCols.size());

    if (view == null) {
      result = new BeeRowSet(rsCols);
      for (int i = 0; i < rsCols.size(); i++) {
        indexes.add(i + 1);
      }

    } else {
      result = new BeeRowSet();

      for (int i = 0; i < rsCols.size(); i++) {
        BeeColumn column = rsCols.get(i);
        String colName = column.getId();
        int colIndex = i + 1;

        if (view.hasColumn(colName)) {
          view.initColumn(colName, column);
          result.addColumn(column);
          indexes.add(colIndex);

        } else if (BeeUtils.same(colName, view.getSourceIdName())) {
          idIndex = colIndex;

        } else if (BeeUtils.same(colName, view.getSourceVersionName())) {
          versionIndex = colIndex;

        } else if (BeeUtils.same(colName, EDITABLE_STATE_COLUMN)) {
          editableIndex = colIndex;
        }
      }
    }

    long rowId = 0;
    boolean editable = (view != null)
        ? sys.getTable(view.getSourceName()).areRecordsEditable() : true;

    int cc = result.getNumberOfColumns();

    while (rs.next()) {
      String[] values = new String[cc];

      for (int i = 0; i < cc; i++) {
        BeeColumn column = result.getColumn(i);
        int colIndex = indexes.get(i);

        switch (column.getType()) {
          case BOOLEAN:
            values[i] = BooleanValue.pack(rs.getBoolean(colIndex));
            if (rs.wasNull()) {
              values[i] = null;
            }
            break;
          case DATE:
            Long time = BeeUtils.toLongOrNull(rs.getString(colIndex));
            values[i] = (time == null) ? null : BeeUtils.toString(time / TimeUtils.MILLIS_PER_DAY);
            break;
          case NUMBER:
          case DECIMAL:
            if (column.getScale() > 0) {
              values[i] = BeeUtils.removeTrailingZeros(rs.getString(colIndex));
            } else {
              values[i] = rs.getString(colIndex);
            }
            break;
          default:
            values[i] = rs.getString(colIndex);
        }
      }

      if (idIndex > 0) {
        rowId = rs.getLong(idIndex);
      } else {
        rowId++;
      }

      BeeRow row;
      if (versionIndex > 0) {
        row = new BeeRow(rowId, rs.getLong(versionIndex), values);
      } else {
        row = new BeeRow(rowId, values);
      }

      if (editableIndex > 0) {
        editable = BeeUtils.toBoolean(rs.getString(editableIndex));
      }
      row.setEditable(editable);

      result.addRow(row);
    }

    if (idIndex > 0) {
      result.setViewName(view.getName());
    }
    logger.debug("cols:", cc, "rows:", result.getNumberOfRows());
    return result;
  }

  private static SimpleRowSet rsToSimpleRowSet(ResultSet rs) throws SQLException {
    List<BeeColumn> rsCols = JdbcUtils.getColumns(rs);
    int cc = rsCols.size();
    String[] columns = new String[cc];

    for (int i = 0; i < cc; i++) {
      columns[i] = rsCols.get(i).getId();
    }
    SimpleRowSet res = new SimpleRowSet(columns);

    while (rs.next()) {
      String[] row = new String[cc];

      for (int i = 0; i < cc; i++) {
        row[i] = rs.getString(i + 1);
      }
      res.addRow(row);
    }
    logger.debug("cols:", res.getNumberOfColumns(), "rows:", res.getNumberOfRows());
    return res;
  }
}
