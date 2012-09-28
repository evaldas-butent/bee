package com.butent.bee.server;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.DataServiceBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.ui.UiHolderBean;
import com.butent.bee.server.ui.UiServiceBean;
import com.butent.bee.server.utils.Reflection;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Receives a service request and then passes it along, depending on service name structure, to an
 * according class for execution.
 */

@Stateless
public class DispatcherBean {
  private static BeeLogger logger = LogUtils.getLogger(DispatcherBean.class);

  @EJB
  ModuleHolderBean moduleBean;
  @EJB
  SystemServiceBean sysBean;
  @EJB
  DataServiceBean dataBean;
  @EJB
  UiServiceBean uiBean;
  @EJB
  UiHolderBean ui;
  @EJB
  Invocation invBean;
  @EJB
  UserServiceBean usrBean;

  public ResponseObject doLogin(String locale, String host, String agent) {
    if (BeeUtils.isEmpty(SqlBuilderFactory.getDsn())) {
      return ResponseObject.error("DSN not specified");
    }
    return usrBean.login(locale, host, agent);
  }

  public void doLogout(String user) {
    usrBean.logout(user);
  }

  public ResponseObject doService(String svc, String dsn, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);
    ResponseObject response = null;

    if (!BeeUtils.isEmpty(dsn) && !BeeUtils.same(SqlBuilderFactory.getDsn(), dsn)) {
      response = ResponseObject.error("DSN mismatch:", SqlBuilderFactory.getDsn(), "!=", dsn);
    } else {
      if (moduleBean.hasModule(svc)) {
        response = moduleBean.doModule(reqInfo);

      } else if (Service.isDbService(svc)) {
        dataBean.doService(svc, dsn, reqInfo, buff);

      } else if (Service.isSysService(svc)) {
        response = sysBean.doService(svc, reqInfo, buff);

      } else if (BeeUtils.same(svc, Service.LOAD_MENU)) {
        response = ui.getMenu();

      } else if (BeeUtils.same(svc, Service.WHERE_AM_I)) {
        buff.addLine(buff.now(), BeeConst.whereAmI());

      } else if (BeeUtils.same(svc, Service.INVOKE)) {
        Reflection.invoke(invBean, reqInfo.getParameter(Service.RPC_VAR_METH), reqInfo, buff);

      } else if (Service.isDataService(svc)) {
        response = uiBean.doService(reqInfo);

      } else {
        String msg = BeeUtils.concat(1, svc, "service type not recognized");
        logger.warning(msg);
        buff.addWarning(msg);
      }
    }
    return response;
  }
}
