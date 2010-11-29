package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.DataSourceBean;
import com.butent.bee.egg.server.jdbc.JdbcUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.egg.shared.sql.IsExpression;
import com.butent.bee.egg.shared.sql.IsFrom;
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
import java.util.HashSet;
import java.util.Set;
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

  public Set<String> dbForeignKeys(String table) {
    BeeRowSet res = (BeeRowSet) processSql(SqlUtils.dbForeignKeys(table).getQuery());

    if (res.isEmpty()) {
      return null;
    }
    Set<String> dbforeignKeys = new HashSet<String>();

    for (BeeRow row : res.getRows()) {
      dbforeignKeys.add(row.getValue(0));
    }
    return dbforeignKeys;
  }

  public Set<String> dbTables(String table) {
    BeeRowSet res = (BeeRowSet) processSql(SqlUtils.dbTables(table).getQuery());

    if (res.isEmpty()) {
      return null;
    }
    Set<String> dbTables = new HashSet<String>();

    for (BeeRow row : res.getRows()) {
      dbTables.add(row.getValue(0));
    }
    return dbTables;
  }

  public BeeRowSet getData(SqlSelect ss) {
    Assert.notNull(ss);
    Assert.state(!ss.isEmpty());

    BeeRowSet res = (BeeRowSet) processSql(ss.getQuery());

    if (BeeUtils.isEmpty(ss.getUnion())) {
      String mainSource = null;
      String mainExt = null;

      for (IsFrom from : ss.getFrom()) {
        if (!(from.getSource() instanceof String)) {
          continue;
        }
        String source = (String) from.getSource();

        if (sys.isTable(source)) {
          if (BeeUtils.isEmpty(mainSource)) {
            mainSource = source;

            if (!BeeUtils.isEmpty(sys.getExtTable(mainSource))) {
              mainExt = sys.getExtTable(mainSource).getName();
            }
          } else if (BeeUtils.isEmpty(mainExt)) {
            break;
          } else if (!BeeUtils.same(source, mainExt)) {
            continue;
          } else {
            mainExt = null;
          }
          Set<String> fieldList = sys.getFieldNames(source);
          fieldList.add(sys.getIdName(source));
          fieldList.add(sys.getLockName(source));
          String alias = from.getAlias();

          for (IsExpression[] pair : ss.getFields()) {
            String xpr = pair[0].getValue();
            IsExpression als = pair[1];

            int idx = xpr.lastIndexOf(".");
            if (idx <= 0) {
              continue;
            }
            String tbl = BeeUtils.left(xpr, idx);
            String fld = xpr.substring(idx + 1);

            if (BeeUtils.same(tbl, BeeUtils.ifString(alias, source))) {
              if (BeeUtils.same(fld, "*")) {
                for (String field : fieldList) {
                  BeeColumn col = res.getColumn(field);
                  col.setFieldSource(mainSource);
                  col.setFieldName(field);
                }
              } else {
                if (fieldList.contains(fld)) {
                  BeeColumn col = res.getColumn(BeeUtils.isEmpty(als) ? fld : als.getValue());
                  col.setFieldSource(mainSource);
                  col.setFieldName(fld);
                }
              }
            }
          }
        }
      }
      res.setSource(mainSource);
    }
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

  public boolean isDbTable(String table) {
    Assert.notEmpty(table);
    return !BeeUtils.isEmpty(dbTables(table));
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
