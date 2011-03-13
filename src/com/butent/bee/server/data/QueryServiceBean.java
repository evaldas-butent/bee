package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.sql.IsQuery;
import com.butent.bee.shared.sql.SqlBuilderFactory;
import com.butent.bee.shared.sql.SqlInsert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

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
import javax.sql.DataSource;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class QueryServiceBean {

  private interface SqlHandler<T> {

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
    BeeRowSet res = getViewData(new SqlSelect()
        .addAllFields(table).addFrom(table).setWhere(SqlUtils.sqlFalse()), null);

    return res.getColumnLabels();
  }

  public List<String[]> dbForeignKeys(String dbName, String dbSchema, String table,
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

  public String[] getColumn(IsQuery query) {
    List<String[]> res = getData(query);
    String[] column = new String[res.size()];

    if (!BeeUtils.isEmpty(res)) {
      Assert.state(res.get(0).length == 1, "Result must contain exactly one column");

      for (int i = 0; i < res.size(); i++) {
        column[i] = res.get(i)[0];
      }
    }
    return column;
  }

  public List<String[]> getData(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    activateTables(query);

    return processSql(query.getQuery(), new SqlHandler<List<String[]>>() {
      @Override
      public List<String[]> processResultSet(ResultSet rs) throws SQLException {
        return rsToList(rs);
      }

      @Override
      public List<String[]> processUpdateCount(int updateCount) {
        return null;
      }
    });
  }

  public int getInt(IsQuery query) {
    return BeeUtils.toInt(getValue(query));
  }

  public long getLong(IsQuery query) {
    return BeeUtils.toLong(getValue(query));
  }

  public String[] getRow(IsQuery query) {
    List<String[]> res = getData(query);
    String[] row = null;

    switch (res.size()) {
      case 0:
        row = new String[0];
        break;

      case 1:
        row = res.get(0);
        break;

      default:
        Assert.untouchable("Result must contain exactly one row");
    }
    return row;
  }

  public Map<String, String> getRowAsMap(SqlSelect ss) {
    BeeRowSet rs = getViewData(ss, null);
    Assert.isTrue(rs.getNumberOfRows() == 1, "Result must contain exactly one row");
    Map<String, String> values = Maps.newHashMap();
    BeeRow row = rs.getRow(0);

    for (int i = 0; i < rs.getNumberOfColumns(); i++) {
      values.put(rs.getColumnId(i).toLowerCase(), row.getString(i));
    }
    return values;
  }

  public String getValue(IsQuery query) {
    String[] row = getRow(query);

    Assert.isTrue(row.length == 1, "Result must contain exactly one column");
    return row[0];
  }

  public BeeRowSet getViewData(SqlSelect ss, final BeeView view) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    activateTables(ss);

    if (!BeeUtils.isEmpty(view)) {
      String tableName = view.getSource();

      ss.addField(tableName, sys.getIdName(tableName), SqlUtils.uniqueName())
          .addField(tableName, sys.getLockName(tableName), SqlUtils.uniqueName());
    }
    return processSql(ss.getQuery(), new SqlHandler<BeeRowSet>() {
      @Override
      public BeeRowSet processResultSet(ResultSet rs) throws SQLException {
        return rsToBeeRowSet(rs, view);
      }

      @Override
      public BeeRowSet processUpdateCount(int updateCount) {
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
    int cols = BeeUtils.isEmpty(view) ? rsCols.length : view.getFields().size();
    BeeColumn[] columns = new BeeColumn[cols];

    int idIndex = -1;
    int verIndex = -1;

    int j = 0;
    for (BeeColumn col : rsCols) {
      if (!BeeUtils.isEmpty(view)) {
        BeeField field = view.getFields().get(col.getId());

        if (BeeUtils.isEmpty(field)) {
          if (idIndex < 0) {
            idIndex = col.getIndex();
          } else {
            verIndex = col.getIndex();
          }
          continue;
        }
        switch (field.getType()) {
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
            row[i] = BooleanValue.serialize(rs.getBoolean(columns[i].getIndex()));
            break;
          case NUMBER:
            if (columns[i].getScale() > 0) {
              row[i] = BeeUtils.removeTrailingZeros(rs.getString(columns[i].getIndex()));
              break;
            }
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

  public List<String[]> rsToList(ResultSet rs) throws SQLException {
    BeeColumn[] rsCols = JdbcUtils.getColumns(rs);
    int cols = rsCols.length;
    List<String[]> res = Lists.newArrayListWithCapacity(cols);

    while (rs.next()) {
      String[] row = new String[cols];

      for (int i = 0; i < cols; i++) {
        row[i] = rs.getString(rsCols[i].getIndex());
      }
      res.add(row);
    }
    LogUtils.info(logger, "Retrieved rows:", res.size());
    return res;
  }

  public int updateData(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());
    Assert.state(!(query instanceof SqlSelect));

    activateTables(query);

    Integer res = processSql(query.getQuery(), new SqlHandler<Integer>() {
      @Override
      public Integer processResultSet(ResultSet rs) throws SQLException {
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
      LogUtils.error(logger, ex);
    } finally {
      JdbcUtils.closeResultSet(rs);
      JdbcUtils.closeStatement(stmt);
      JdbcUtils.closeConnection(con);
    }
    return result;
  }
}
