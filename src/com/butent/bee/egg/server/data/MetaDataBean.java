package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.jdbc.JdbcException;
import com.butent.bee.egg.server.utils.BeeDataSource;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;
import com.butent.bee.egg.shared.utils.SubProp;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class MetaDataBean {
  private static Logger logger = Logger.getLogger(MetaDataBean.class.getName());

  @EJB
  ResultSetBean rsb;

  public void doService(String svc, BeeDataSource ds, RequestInfo reqInfo,
      ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(ds);
    Assert.notNull(buff);

    if (BeeService.equals(svc, BeeService.SERVICE_DB_PING)) {
      ping(ds, buff);
    } else if (BeeService.equals(svc, BeeService.SERVICE_DB_INFO)) {
      dbInfo(ds, buff);
    } else if (BeeService.equals(svc, BeeService.SERVICE_DB_TABLES)) {
      getTables(ds, reqInfo, buff);
    } else {
      String msg = BeeUtils.concat(1, svc, "meta data service not recognized");
      LogUtils.warning(logger, msg);
      buff.addWarning(msg);
    }
  }

  private void dbInfo(BeeDataSource ds, ResponseBuffer buff) {
    List<SubProp> prp = null;
    boolean ok = true;

    try {
      prp = ds.getDbInfo();
    } catch (SQLException ex) {
      LogUtils.error(logger, ex);
      buff.addError(ex);
      ok = false;
    }

    if (prp.isEmpty()) {
      if (ok) {
        buff.addLine(ds.getTp(), "no info available");
      }
      return;
    }

    buff.addSub(prp);
  }

  private void getTables(BeeDataSource ds, RequestInfo reqInfo,
      ResponseBuffer buff) {
    try {
      DatabaseMetaData md = ds.getDbMd();
      ResultSet rs = md.getTables(null, null, null, null);

      rsb.rsToResponse(rs, buff, reqInfo.isDebug());

      rs.close();
    } catch (JdbcException ex) {
      LogUtils.error(logger, ex);
    } catch (SQLException ex) {
      LogUtils.error(logger, ex);
    }
  }

  private void ping(BeeDataSource ds, ResponseBuffer buff) {
    buff.add(ds.toString());
  }

}
