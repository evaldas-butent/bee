package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.DataSourceBean;
import com.butent.bee.egg.server.jdbc.JdbcUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlBuilder;
import com.butent.bee.egg.shared.sql.SqlBuilderFactory;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
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

  DataSource ds = null;
  @EJB
  DataSourceBean dsb;
  @EJB
  IdGeneratorBean ig;

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

  public BeeRowSet getTables() {
    SqlBuilder builder = SqlBuilderFactory.getBuilder();
    return (BeeRowSet) processSql(builder.getTables());
  }

  public long insertData(SqlInsert si) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());

    long id = ig.getId((String) si.getTarget().getSource());

    if (!BeeUtils.isEmpty(id)) {
      si.addField("version", 1).addField("id", id);
    }

    int cnt = updateData(si);

    if (BeeUtils.isEmpty(cnt)) {
      return -1;
    }
    return id;
  }

  public Object processSql(String sql) {
    Assert.notEmpty(sql);

    ds = dsb.locateDs(SqlBuilderFactory.getEngine()).getDs();

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

  public int setIsolationLevel(int level) {
    if (!BeeUtils.isPositive(level)) {
      return level;
    }
    ds = dsb.locateDs(SqlBuilderFactory.getEngine()).getDs();
    Connection con = null;
    int oldLevel = -1;

    try {
      con = ds.getConnection();

      if (con.getMetaData().supportsTransactionIsolationLevel(level)) {
        oldLevel = con.getTransactionIsolation();

        if (oldLevel != level) {
          con.setTransactionIsolation(level);
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException("Cannot perform query: " + ex, ex);
    } finally {
      try {
        con.close();
        con = null;
      } catch (SQLException ex) {
        throw new RuntimeException("Cannot close connection: " + ex, ex);
      }
    }
    return oldLevel;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  public void switchEngine(String dsn) {
    String oldDsn = SqlBuilderFactory.getEngine();

    if (!BeeUtils.same(oldDsn, dsn)) {
      ig.destroy();
      SqlBuilderFactory.setDefaultEngine(dsn);
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
