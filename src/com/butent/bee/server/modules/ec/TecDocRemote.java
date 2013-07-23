package com.butent.bee.server.modules.ec;

import com.google.common.collect.Lists;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.QueryServiceBean.ResultSetProcessor;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

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

  private DataSource dataSource;

  public void cleanup(List<Pair<SqlCreate, String>> init) {
    for (Pair<SqlCreate, String> entry : init) {
      processSql("DROP TABLE IF EXISTS " + entry.getA().getTarget());
    }
  }

  public void init(List<Pair<SqlCreate, String>> init) {
    DataSource ds = getDataSource();
    SqlBuilder builder = SqlBuilderFactory.getBuilder(qs.dbEngine(ds));

    for (Pair<SqlCreate, String> entry : init) {
      SqlCreate query = entry.getA();
      String index = entry.getB();
      String table = query.getTarget();

      processSql("DROP TABLE IF EXISTS " + table);
      processSql(query.getSqlString(builder));

      if (!BeeUtils.isEmpty(index)) {
        processSql(SqlUtils.createIndex(query.getTarget(), SqlUtils.uniqueName(),
            Lists.newArrayList(index), false).getSqlString(builder));
      }
    }
  }

  public List<StringBuilder> getRemoteData(SqlSelect query, final SqlInsert insert) {
    return qs.getData(getDataSource(), query, new ResultSetProcessor<List<StringBuilder>>() {
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
            Object value;

            if (rs.getMetaData().getColumnType(i + 1) == -4 /* LONGBLOB */) {
              value = Codec.toBase64(rs.getBytes(field));
            } else {
              value = rs.getObject(field);
            }
            sb.append(SqlUtils.constant(value).getSqlString(builder));
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

  private DataSource getDataSource() {
    if (dataSource == null) {
      try {
        dataSource = (DataSource) InitialContext.doLookup("jdbc/tcd");
      } catch (NamingException ex) {
        try {
          dataSource = (DataSource) InitialContext.doLookup("java:jdbc/tcd");
        } catch (NamingException ex2) {
          logger.error(ex);
          dataSource = null;
        }
      }
    }
    return dataSource;
  }

  private void processSql(String sql) {
    DataSource ds = getDataSource();
    Assert.notNull(ds);

    Object response = qs.doSql(ds, sql);

    if (response instanceof String) {
      throw new BeeRuntimeException((String) response);
    }
  }
}
