package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.DataSourceBean;
import com.butent.bee.egg.server.jdbc.JdbcUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlBuilderFactory;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

  public BeeRowSet getData(SqlSelect ss) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    return (BeeRowSet) processSql(ss.getQuery());
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

    if (BeeUtils.isEmpty(si.getSource()) && sys.beeTable(source)) {
      String lockFld = sys.getLockName(source);

      if (!si.hasField(lockFld)) {
        si.addConstant(lockFld, 1);
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
      } catch (SQLException ex) {
        throw new RuntimeException("Cannot close connection: " + ex, ex);
      }
    }
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  public void switchEngine(String dsn) {
    String oldDsn = SqlBuilderFactory.getEngine();

    if (!BeeUtils.same(oldDsn, dsn)) {
      ig.destroy();
      SqlBuilderFactory.setDefaultEngine(dsn);
    }
  }

  public boolean tableExists(String table) {
    BeeRowSet res = tableList();

    if (!res.isEmpty()) {
      for (BeeRow row : res.getRows()) {
        if (BeeUtils.same(row.getValue(0), table)) {
          return true;
        }
      }
    }
    return false;
  }

  public BeeRowSet tableList() {
    return (BeeRowSet) processSql(SqlUtils.getTables().getQuery());
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
