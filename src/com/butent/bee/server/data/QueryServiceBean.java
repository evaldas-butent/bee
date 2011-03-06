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
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.sql.BeeConstants.DataTypes;
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
    BeeRowSet res = (BeeRowSet) processSql(SqlUtils.dbForeignKeys(dbName, dbSchema, table, refTable)
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

  public BeeRowSet getBaseData(String tableName, String sql) {
    Assert.isTrue(sys.isTable(tableName), "Not a base table: " + tableName);
    Assert.notEmpty(sql);
    
    String idName = sys.getIdName(tableName);
    String verName = sys.getLockName(tableName);
    
    Map<String, DataTypes> types = Maps.newHashMap();
    for (BeeField field : sys.getTableFields(tableName)) {
      types.put(field.getName().toLowerCase(), field.getType());
    }

    DataSource ds = dsb.locateDs(SqlBuilderFactory.getEngine()).getDs();

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    BeeRowSet result = null;
    
    LogUtils.info(logger, tableName, sql);

    try {
      con = ds.getConnection();
      stmt = con.createStatement();
      rs = stmt.executeQuery(sql);
      
      BeeColumn[] rsCols = JdbcUtils.getColumns(rs);
      List<BeeColumn> columns = Lists.newArrayList();
      
      int idIndex = -1;
      int verIndex = -1;

      String fieldName;
      DataTypes type;
      for (BeeColumn col : rsCols) {
        fieldName = col.getLabel();
        if (BeeUtils.same(fieldName, idName)) {
          idIndex = col.getIndex();
          continue;
        }
        if (BeeUtils.same(fieldName, verName)) {
          verIndex = col.getIndex();
          continue;
        }
        columns.add(col);
        
        type = types.get(fieldName.toLowerCase());
        if (type == null) {
          continue;
        }
        switch (type) {
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
      
      int cc = columns.size();
      Assert.isPositive(idIndex);
      Assert.isPositive(cc);

      int[] colIndex = new int[cc];
      ValueType[] colType = new ValueType[cc];
      int[] colScale = new int[cc];
      for (int i = 0; i < cc; i++) {
        colIndex[i] = columns.get(i).getIndex();
        colType[i] = columns.get(i).getType();
        colScale[i] = columns.get(i).getScale();
      }

      result = new BeeRowSet(columns);
      result.setViewName(tableName);
      while (rs.next()) {
        String[] row = new String[cc];

        for (int i = 0; i < cc; i++) {
          switch (colType[i]) {
            case BOOLEAN:
              row[i] = BooleanValue.serialize(rs.getBoolean(colIndex[i]));
              break;
            case NUMBER:
              if (colScale[i] > 0) {
                row[i] = BeeUtils.removeTrailingZeros(rs.getString(colIndex[i]));
              } else {
                row[i] = rs.getString(colIndex[i]);
              }
              break;
            default:
              row[i] = rs.getString(colIndex[i]);
          }
        }
        if (verIndex > 0) {
          result.addRow(rs.getLong(idIndex), rs.getLong(verIndex), row);
        } else {
          result.addRow(rs.getLong(idIndex), row);
        }
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
  
  public long insertData(SqlInsert si) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());

    long id = 0;
    String source = (String) si.getTarget().getSource();

    if (BeeUtils.isEmpty(si.getDataSource()) && sys.isTable(source)) {
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

    LogUtils.info(logger, "SQL:", sql);

    try {
      con = ds.getConnection();
      stmt = con.createStatement();

      boolean isResultSet = stmt.execute(sql);

      if (isResultSet) {
        ResultSet rs = stmt.getResultSet();

        BeeRowSet result = new BeeRowSet(JdbcUtils.getColumns(rs));
        int cols = result.getNumberOfColumns();
        
        long id = 0;
        while (rs.next()) {
          String[] row = new String[cols];

          for (int i = 0; i < cols; i++) {
            row[i] = rs.getString(i + 1);
          }
          result.addRow(++id, row);
        }
        LogUtils.info(logger, "Retrieved rows:", result.getNumberOfRows());
        return result;

      } else {
        int result = stmt.getUpdateCount();

        LogUtils.info(logger, "Affected rows:", result);
        return result;
      }

    } catch (SQLException ex) {
      throw new RuntimeException("Cannot perform query: " + ex, ex);
    } finally {
      try {
        stmt.close();
        con.close();
        con = null;
        stmt = null;
      } catch (Exception ex) {
        throw new RuntimeException("Cannot close connection: " + ex, ex);
      }
    }
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
