package com.butent.bee.server.modules.ec;

import com.google.common.collect.Lists;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.QueryServiceBean.ResultSetProcessor;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class TecDocRemote {

  private static BeeLogger logger = LogUtils.getLogger(TecDocRemote.class);
  private static final int MAX_INSERT_BLOCK = 100000;

  @EJB
  QueryServiceBean qs;

  public List<StringBuilder> getRemoteData(SqlSelect query, final SqlInsert insert) {
    DataSource ds;

    try {
      ds = (DataSource) InitialContext.doLookup("jdbc/tcd");
    } catch (NamingException ex) {
      try {
        ds = (DataSource) InitialContext.doLookup("java:jdbc/tcd");
      } catch (NamingException ex2) {
        logger.error(ex);
        ds = null;
      }
    }
    if (ds == null) {
      return Lists.newArrayList();
    }
    return qs.getData(ds, query, new ResultSetProcessor<List<StringBuilder>>() {
      @Override
      public List<StringBuilder> processResultSet(ResultSet rs) throws SQLException {
        SqlBuilder builder = SqlBuilderFactory.getBuilder();
        StringBuilder sb = new StringBuilder();
        int c = 0;
        List<StringBuilder> inserts = Lists.newArrayList();

        while (rs.next()) {
          if (c == 0) {
            sb = new StringBuilder(insert.getSqlString(builder));
          } else {
            sb.append(",");
          }
          sb.append("(");
          int i = 0;

          for (String field : insert.getFields()) {
            if (i > 0) {
              sb.append(",");
            }
            sb.append(SqlUtils.constant(rs.getObject(field)).getSqlString(builder));
            i++;
          }
          sb.append(")");
          c++;

          if (c == MAX_INSERT_BLOCK) {
            inserts.add(sb);
            c = 0;
          }
        }
        if (c > 0) {
          inserts.add(sb);
        }
        return inserts;
      }
    });
  }
}
