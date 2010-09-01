package com.butent.bee.egg.server.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.butent.bee.egg.server.Assert;
import com.butent.bee.egg.server.DataSourceBean;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.http.ResponseBuffer;
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

    String sql = XmlUtils.getText(reqData, BeeService.FIELD_JDBC_QUERY);
    if (BeeUtils.isEmpty(sql)) {
      buff.addLine("Parameter", BeeService.FIELD_JDBC_QUERY, "not found");
      return;
    }

    String ret = XmlUtils.getText(reqData, BeeService.FIELD_JDBC_RETURN);

    boolean debug = reqInfo.isDebug();

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);

      if (BeeConst.JDBC_META_DATA.equals(ret)) {
        rsb.rsMdToResponse(rs, buff, debug);
      } else if (BeeConst.JDBC_ROW_COUNT.equals(ret)) {
        BeeDate start = new BeeDate();
        int rc = JdbcUtils.getSize(rs);
        BeeDate end = new BeeDate();

        buff.addLine(enter.toLog(), start.toLog(), end.toLog());
        buff.addLine(ret, rc, BeeUtils.bracket(BeeUtils.toSeconds(end.getTime()
            - start.getTime())));
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
