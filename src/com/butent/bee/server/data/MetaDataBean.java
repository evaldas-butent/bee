package com.butent.bee.server.data;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.jdbc.JdbcException;
import com.butent.bee.server.utils.BeeDataSource;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Handles rpc_db_meta service requests (database ping, current tables list etc).
 */

@Stateless
public class MetaDataBean {
  private static BeeLogger logger = LogUtils.getLogger(MetaDataBean.class);

  @EJB
  ResultSetBean rsb;

  public void doService(String svc, BeeDataSource ds, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(ds);
    Assert.notNull(buff);

    if (BeeUtils.same(svc, Service.DB_PING)) {
      ping(ds, buff);
    } else if (BeeUtils.same(svc, Service.DB_INFO)) {
      dbInfo(ds, buff);
    } else if (BeeUtils.same(svc, Service.DB_TABLES)) {
      getTables(ds, reqInfo, buff);
    } else if (BeeUtils.same(svc, Service.DB_KEYS)) {
      getKeys(ds, reqInfo, buff, true);
    } else if (BeeUtils.same(svc, Service.DB_PRIMARY)) {
      getKeys(ds, reqInfo, buff, false);
    } else {
      String msg = BeeUtils.joinWords(svc, "meta data service not recognized");
      logger.warning(msg);
      buff.addWarning(msg);
    }
  }

  private void dbInfo(BeeDataSource ds, ResponseBuffer buff) {
    List<ExtendedProperty> prp = null;
    boolean ok = true;

    try {
      prp = ds.getDbInfo();
    } catch (SQLException ex) {
      logger.error(ex);
      buff.addError(ex);
      ok = false;
    }

    if (prp.isEmpty()) {
      if (ok) {
        buff.addLine(ds.getDsn(), "no info available");
      }
      return;
    }

    buff.addExtendedProperties(prp);
  }

  private void getKeys(BeeDataSource ds, RequestInfo reqInfo, ResponseBuffer buff,
      boolean result) {
    String table = reqInfo.getParameter(0);
    String catalog = reqInfo.getParameter(1);
    String schema = reqInfo.getParameter(2);

    try {
      DatabaseMetaData md = ds.getDbMd();
      ResultSet rs = md.getPrimaryKeys(catalog, schema, table);

      if (result) {
        rsb.rsToResponse(rs, buff, reqInfo.isDebug());
      } else {
        while (rs.next()) {
          buff.add(rs.getString("COLUMN_NAME"));
        }
      }

      rs.close();
    } catch (JdbcException ex) {
      logger.error(ex);
    } catch (SQLException ex) {
      logger.error(ex);
    }
  }

  private void getTables(BeeDataSource ds, RequestInfo reqInfo, ResponseBuffer buff) {
    try {
      DatabaseMetaData md = ds.getDbMd();
      ResultSet rs = md.getTables(null, null, null, null);

      rsb.rsToResponse(rs, buff, reqInfo.isDebug());

      rs.close();
    } catch (JdbcException ex) {
      logger.error(ex);
    } catch (SQLException ex) {
      logger.error(ex);
    }
  }

  private void ping(BeeDataSource ds, ResponseBuffer buff) {
    buff.add(ds.toString());
  }

}
