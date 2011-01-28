package com.butent.bee.server.data;

import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.jdbc.JdbcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.BeeRowSet.BeeRow;
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

    return res.getColumnNames();
  }

  public Collection<String[]> dbForeignKeys(String dbName, String dbSchema, String table,
      String refTable) {
    List<String[]> dbforeignKeys = new ArrayList<String[]>();
    BeeRowSet res = (BeeRowSet) processSql(SqlUtils.dbForeignKeys(dbName, dbSchema, table, refTable)
        .getQuery());

    if (!res.isEmpty()) {
      for (BeeRow row : res.getRows()) {
        dbforeignKeys.add(new String[]{row.getValue(0), row.getValue(1), row.getValue(2)});
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
    String[] dbTables = new String[res.getRowCount()];

    if (!res.isEmpty()) {
      int i = 0;

      for (BeeRow row : res.getRows()) {
        dbTables[i++] = row.getValue(0);
      }
    }
    return dbTables;
  }

  public BeeRowSet getData(SqlSelect ss) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    BeeRowSet res = (BeeRowSet) processSql(ss.getQuery());
    return res;
  }

  public BeeRow getSingleRow(SqlSelect ss) {
    BeeRowSet rs = getData(ss);
    Assert.isTrue(rs.getRowCount() == 1, "Result must contain exactly one row");
    return rs.getRow(0);
  }

  public long insertData(SqlInsert si) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());

    long id = 0;
    String source = (String) si.getTarget().getSource();

    if (BeeUtils.isEmpty(si.getSource()) && sys.isTable(source)) {
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
        int cols = result.getColumnCount();

        while (rs.next()) {
          String[] row = new String[cols];

          for (int i = 0; i < cols; i++) {
            row[i] = rs.getString(i + 1);
          }
          result.addRow(row);
        }
        LogUtils.info(logger, "Retrieved rows:", result.getRowCount());
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
      return (Integer) processSql(query.getQuery());
    }
  }
}
