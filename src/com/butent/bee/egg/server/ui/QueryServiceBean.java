package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.server.DataSourceBean;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

  DataSource ds = null;
  @EJB
  DataSourceBean dsb;
  @EJB
  IdGeneratorBean ig;

  @SuppressWarnings("unchecked")
  public List<Object[]> getData(SqlSelect ss) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    return (List<Object[]>) processSql(ss.getQuery());
  }

  public long insertData(SqlInsert si) {
    Assert.notNull(si);
    Assert.state(!si.isEmpty());

    long id = ig.getId((String) si.getTarget().getSource());

    if (!BeeUtils.isEmpty(id)) {
      si.addField("version", 1).addField("id", id);
    }

    int cnt = processUpdate(si);

    if (BeeUtils.isEmpty(cnt)) {
      return -1;
    }
    return id;
  }

  public Object processSql(String sql) {
    if (ds == null) {
      ds = dsb.locateDs(BeeConst.MYSQL).getDs();
    }
    Connection con = null;
    Statement stmt = null;

    LogUtils.info(logger, "SQL:", sql);

    try {
      con = ds.getConnection();
      stmt = con.createStatement();

      boolean isResultSet = stmt.execute(sql);

      if (isResultSet) {
        List<Object[]> result = new ArrayList<Object[]>();

        ResultSet rs = stmt.getResultSet();
        int cols = rs.getMetaData().getColumnCount();

        while (rs.next()) {
          Object[] o = new Object[cols];

          for (int i = 0; i < cols; i++) {
            o[i] = rs.getObject(i + 1);
          }
          result.add(o);
        }
        LogUtils.info(logger, "Retrieved rows:", result.size());
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

  public int processUpdate(IsQuery query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());

    if (query instanceof SqlSelect) {
      return -1;
    } else {
      return (Integer) processSql(query.getQuery());
    }
  }
}
