package com.butent.bee.egg.server.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.butent.bee.egg.server.Assert;
import com.butent.bee.egg.server.DataSourceBean;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.server.jdbc.BeeConnection;
import com.butent.bee.egg.server.jdbc.BeeResultSet;
import com.butent.bee.egg.server.jdbc.JdbcException;
import com.butent.bee.egg.server.jdbc.JdbcUtils;
import com.butent.bee.egg.server.utils.BeeDataSource;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

@Stateless
public class DataServiceBean {
  private static Logger logger = Logger.getLogger(DataServiceBean.class
      .getName());

  @EJB
  DataSourceBean dsb;
  @EJB
  MetaDataBean mdb;
  @EJB
  ResultSetBean rsb;

  public void doService(String svc, String dsn, RequestInfo reqInfo,
      ResponseBuffer buff) {
    Assert.notEmpty(svc);

    BeeDataSource ds = checkDs(dsn, buff);
    if (ds == null)
      return;

    if (BeeService.isDbMetaService(svc))
      mdb.doService(svc, ds, reqInfo, buff);
    else if (svc.equals(BeeService.SERVICE_DB_JDBC))
      testJdbc(ds.getConn(), reqInfo, buff);
    else {
      String msg = BeeUtils.concat(1, svc, dsn, "data service not recognized");
      LogUtils.warning(logger, msg);
      buff.add(msg);
    }

    ds.close();
  }

  private BeeDataSource checkDs(String dsn, ResponseBuffer buff) {
    if (BeeUtils.isEmpty(dsn)) {
      buff.add("dsn not specified");
      return null;
    }

    BeeDataSource z = dsb.locateDs(dsn);

    if (z == null) {
      buff.addLine(dsn, "not found");
      return null;
    }
    if (!z.check()) {
      buff.addLine("cannot open", dsn, z.getErrors());
      return null;
    }

    return z;
  }

  private void testJdbc(Connection conn, RequestInfo reqInfo,
      ResponseBuffer buff) {
    BeeDate enter = new BeeDate();

    String reqData = reqInfo.getContent();
    if (BeeUtils.isEmpty(reqData)) {
      buff.add("Request data not found");
      return;
    }

    Map<String, String> map = XmlUtils.getText(reqData);

    String sql = map.get(BeeService.FIELD_JDBC_QUERY);
    if (BeeUtils.isEmpty(sql)) {
      buff.addLine("Parameter", BeeService.FIELD_JDBC_QUERY, "not found");
      return;
    }

    for (String key : map.keySet())
      if (BeeConst.isDefault(map.get(key)))
        map.put(key, BeeConst.STRING_EMPTY);

    String cAc = map.get(BeeService.FIELD_CONNECTION_AUTO_COMMIT);
    String cH = map.get(BeeService.FIELD_CONNECTION_HOLDABILITY);
    String cRo = map.get(BeeService.FIELD_CONNECTION_READ_ONLY);
    String cTi = map.get(BeeService.FIELD_CONNECTION_TRANSACTION_ISOLATION);

    String sCn = map.get(BeeService.FIELD_STATEMENT_CURSOR_NAME);
    String sEp = map.get(BeeService.FIELD_STATEMENT_ESCAPE_PROCESSING);
    String sFd = map.get(BeeService.FIELD_STATEMENT_FETCH_DIRECTION);
    String sFs = map.get(BeeService.FIELD_STATEMENT_FETCH_SIZE);
    String sMf = map.get(BeeService.FIELD_STATEMENT_MAX_FIELD_SIZE);
    String sMr = map.get(BeeService.FIELD_STATEMENT_MAX_ROWS);
    String sP = map.get(BeeService.FIELD_STATEMENT_POOLABLE);
    String sQt = map.get(BeeService.FIELD_STATEMENT_QUERY_TIMEOUT);
    String sRc = map.get(BeeService.FIELD_STATEMENT_RS_CONCURRENCY);
    String sRh = map.get(BeeService.FIELD_STATEMENT_RS_HOLDABILITY);
    String sRt = map.get(BeeService.FIELD_STATEMENT_RS_TYPE);

    String rFd = map.get(BeeService.FIELD_RESULT_SET_FETCH_DIRECTION);
    String rFs = map.get(BeeService.FIELD_RESULT_SET_FETCH_SIZE);

    String ret = map.get(BeeService.FIELD_JDBC_RETURN);
    boolean debug = reqInfo.isDebug();

    String before = "before:";
    String after = "after:";

    boolean b1, b2;
    int i1, i2;

    BeeConnection bc = new BeeConnection(conn);

    if (BeeUtils.isBoolean(cAc)) {
      b1 = bc.isAutoCommit();
      bc.updateAutoCommit(conn, cAc);
      i2 = bc.getAutoCommitQuietly(conn);

      if (bc.hasErrors()) {
        bc.revert(conn);
        buff.addErrors(bc.getErrors());
        return;
      }

      buff.addMessage("Connection Auto Commit:", cAc, before, b1, after,
          BeeUtils.toBoolean(i2));
    }

    if (!BeeUtils.isEmpty(cH)) {
      i1 = bc.getHoldability();
      bc.updateHoldability(conn, cH);
      i2 = bc.getHoldabilityQuietly(conn);

      if (bc.hasErrors()) {
        bc.revert(conn);
        buff.addErrors(bc.getErrors());
        return;
      }

      buff.addMessage("Connection Holdability:", cH, before, i1,
          JdbcUtils.holdabilityAsString(i1), after, i2,
          JdbcUtils.holdabilityAsString(i2));
    }

    if (!BeeUtils.isEmpty(cTi)) {
      i1 = bc.getTransactionIsolation();
      bc.updateTransactionIsolation(conn, cTi);
      i2 = bc.getTransactionIsolationQuietly(conn);

      if (bc.hasErrors()) {
        bc.revert(conn);
        buff.addErrors(bc.getErrors());
        return;
      }

      buff.addMessage("Connection Transaction Isolation:", cTi, before, i1,
          JdbcUtils.transactionIsolationAsString(i1), after, i2,
          JdbcUtils.transactionIsolationAsString(i2));
    }

    if (BeeUtils.isBoolean(cRo)) {
      b1 = bc.isReadOnly();
      bc.updateReadOnly(conn, cRo);
      i2 = bc.getReadOnlyQuietly(conn);

      if (bc.hasErrors()) {
        bc.revert(conn);
        buff.addErrors(bc.getErrors());
        return;
      }

      buff.addMessage("Connection Read Only:", cRo, before, b1, after,
          BeeUtils.toBoolean(i2));
    }

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);

      if (BeeConst.JDBC_COLUMNS.equals(ret)) {
        rsb.rsMdToResponse(rs, buff, debug);
      } else if (BeeConst.JDBC_ROW_COUNT.equals(ret)) {
        BeeDate start = new BeeDate();
        int rc = JdbcUtils.getSize(rs);
        BeeDate end = new BeeDate();

        buff.addLine(enter.toLog(), start.toLog(), end.toLog());
        buff.addLine(ret, rc, BeeUtils.bracket(BeeUtils.toSeconds(end.getTime()
            - start.getTime())));
      } else if (BeeConst.JDBC_META_DATA.equals(ret)) {
        buff.addSub(new BeeResultSet(rs).getRsInfo());
      } else {
        rsb.rsToResponse(rs, buff, debug);
      }

      rs.close();
      stmt.close();
    } catch (JdbcException ex) {
      LogUtils.error(logger, ex);
      buff.add(ex.toString());
    } catch (SQLException ex) {
      LogUtils.error(logger, ex);
      buff.add(ex.toString());
    }

  }

}
