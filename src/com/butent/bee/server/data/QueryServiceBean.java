package com.butent.bee.server.data;

import com.google.common.collect.Maps;

import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
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
import java.util.ArrayList;
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

  private static Logger logger = Logger.getLogger(QueryServiceBean.class.getName());

  @EJB
  DataSourceBean dsb;
  @EJB
  IdGeneratorBean ig;
  @EJB
  SystemBean sys;

  public String[] dbFields(String table) {
    BeeRowSet res = getData(new SqlSelect()
        .addAllFields(table).addFrom(table).setWhere(SqlUtils.sqlFalse()));

    return res.getColumnLabels();
  }

  public Collection<String[]> dbForeignKeys(String dbName, String dbSchema, String table,
      String refTable) {
    List<String[]> dbforeignKeys = new ArrayList<String[]>();
    BeeRowSet res =
        (BeeRowSet) processSql(SqlUtils.dbForeignKeys(dbName, dbSchema, table, refTable)
            .getQuery());

    if (!res.isEmpty()) {
      for (IsRow row : res.getRows()) {
        dbforeignKeys.add(new String[]{row.getString(0), row.getString(1), row.getString(2)});
      }
    }
    return dbforeignKeys;
  }

  public String dbName() {
    String sql = SqlUtils.dbName().getQuery();

    if (!BeeUtils.isEmpty(sql)) {
      return ((BeeRowSet) processSql(sql)).getRow(0).getString(0);
    }
    return "";
  }

  public String dbSchema() {
    String sql = SqlUtils.dbSchema().getQuery();

    if (!BeeUtils.isEmpty(sql)) {
      return ((BeeRowSet) processSql(sql)).getRow(0).getString(0);
    }
    return "";
  }

  public String[] dbTables(String dbName, String dbSchema, String table) {
    BeeRowSet res = (BeeRowSet) processSql(SqlUtils.dbTables(dbName, dbSchema, table).getQuery());
    String[] dbTables = new String[res.getNumberOfRows()];

    if (!res.isEmpty()) {
      int i = 0;

      for (IsRow row : res.getRows()) {
        dbTables[i++] = row.getString(0);
      }
    }
    return dbTables;
  }

  public BeeRowSet getData(SqlSelect ss) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    activateTables(ss);

    return (BeeRowSet) processSql(ss.getQuery());
  }

  public long getLong(SqlSelect ss, String columnId) {
    BeeRowSet rs = getData(ss);
    Assert.isTrue(rs.getNumberOfRows() == 1, "Result must contain exactly one row");
    return rs.getLong(rs.getRow(0), columnId);
  }

  public Map<String, String> getRowValues(SqlSelect ss) {
    BeeRowSet rs = getData(ss);
    Assert.isTrue(rs.getNumberOfRows() == 1, "Result must contain exactly one row");
    Map<String, String> values = Maps.newHashMap();
    BeeRow row = rs.getRow(0);
    for (int i = 0; i < rs.getNumberOfColumns(); i++) {
      values.put(rs.getColumnId(i).toLowerCase(), row.getString(i));
    }
    return values;
  }

  public BeeRow getSingleRow(SqlSelect ss) {
    BeeRowSet rs = getData(ss);
    Assert.isTrue(rs.getNumberOfRows() == 1, "Result must contain exactly one row");
    return rs.getRow(0);
  }

  public String getString(SqlSelect ss, String columnId) {
    BeeRowSet rs = getData(ss);
    Assert.isTrue(rs.getNumberOfRows() == 1, "Result must contain exactly one row");
    return rs.getString(rs.getRow(0), columnId);
  }

  public BeeRowSet getViewData(SqlSelect ss, BeeView view) {
    if (BeeUtils.isEmpty(view)) {
      return getData(ss);
    }
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    activateTables(ss);

    String tableName = view.getSource();

    String sql = ss
        .addField(tableName, sys.getIdName(tableName), SqlUtils.uniqueName())
        .addField(tableName, sys.getLockName(tableName), SqlUtils.uniqueName())
        .getQuery();

    DataSource ds = dsb.locateDs(SqlBuilderFactory.getEngine()).getDs();

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    BeeRowSet result = null;

    LogUtils.info(logger, "SQL:", sql);

    try {
      con = ds.getConnection();
      stmt = con.createStatement();
      rs = stmt.executeQuery(sql);

      int cols = view.getFields().size();
      BeeColumn[] rsCols = JdbcUtils.getColumns(rs);
      BeeColumn[] columns = new BeeColumn[cols];
      System.arraycopy(rsCols, 0, columns, 0, cols);

      int j = 0;
      for (BeeField field : view.getFields().values()) {
        BeeColumn col = columns[j++];

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
      result = new BeeRowSet(columns);
      result.setViewName(tableName);

      while (rs.next()) {
        String[] row = new String[cols];

        for (int i = 0; i < cols; i++) {
          row[i] = rs.getString(columns[i].getIndex());

          switch (columns[i].getType()) {
            case BOOLEAN:
              row[i] = BooleanValue.serialize(rs.getBoolean(columns[i].getIndex()));
              break;
            case NUMBER:
              if (columns[i].getScale() > 0) {
                row[i] = BeeUtils.removeTrailingZeros(row[i]);
              }
              break;
            default:
          }
        }
        result.addRow(rs.getLong(cols + 1), rs.getLong(cols + 2), row);
      }
      LogUtils.info(logger, tableName, "cols:", result.getNumberOfColumns(),
          "rows:", result.getNumberOfRows());

    } catch (SQLException ex) {
      LogUtils.error(logger, ex);
    } finally {
      JdbcUtils.closeResultSet(rs);
      JdbcUtils.closeStatement(stmt);
      JdbcUtils.closeConnection(con);
    }
    return result;
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

  public Object processSql(String sql) {
    Assert.notEmpty(sql);

    DataSource ds = dsb.locateDs(SqlBuilderFactory.getEngine()).getDs();

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    Object result = null;

    LogUtils.info(logger, "SQL:", sql);

    try {
      con = ds.getConnection();
      stmt = con.createStatement();

      boolean isResultSet = stmt.execute(sql);

      if (isResultSet) {
        rs = stmt.getResultSet();

        BeeRowSet res = new BeeRowSet(JdbcUtils.getColumns(rs));
        int cols = res.getNumberOfColumns();

        long id = 0;
        while (rs.next()) {
          String[] row = new String[cols];

          for (int i = 0; i < cols; i++) {
            row[i] = rs.getString(i + 1);
          }
          res.addRow(++id, row);
        }
        LogUtils.info(logger, "Retrieved rows:", res.getNumberOfRows());
        result = res;

      } else {
        result = stmt.getUpdateCount();
        LogUtils.info(logger, "Affected rows:", result);
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

  public int updateData(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    if (query instanceof SqlSelect) {
      return -1;
    } else {
      activateTables(query);

      return (Integer) processSql(query.getQuery());
    }
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
}
