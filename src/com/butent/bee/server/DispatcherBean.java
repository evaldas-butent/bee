package com.butent.bee.server;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.DataServiceBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.ui.UiServiceBean;
import com.butent.bee.server.utils.Reflection;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class DispatcherBean {
  private static Logger logger = Logger.getLogger(DispatcherBean.class.getName());

  @EJB
  SystemServiceBean sysBean;
  @EJB
  DataServiceBean dataBean;
  @EJB
  UiServiceBean uiBean;
  @EJB
  MenuProvider menu;
  @EJB
  Invocation invBean;
  @EJB
  UserServiceBean usr;

  public void doService(String svc, String dsn, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

    LogUtils.warning(logger, "USERIS:", usr.getCurrentUser());

    if (BeeService.isDbService(svc)) {
      dataBean.doService(svc, dsn, reqInfo, buff);
    } else if (BeeService.isSysService(svc)) {
      sysBean.doService(svc, reqInfo, buff);

    } else if (BeeUtils.same(svc, BeeService.SERVICE_GET_MENU)) {
      menu.getMenu(reqInfo, buff);
    } else if (BeeUtils.same(svc, BeeService.SERVICE_WHERE_AM_I)) {
      buff.addLine(buff.now(), BeeConst.whereAmI());

    } else if (BeeUtils.same(svc, BeeService.SERVICE_INVOKE)) {
      Reflection.invoke(invBean, reqInfo.getParameter(BeeService.RPC_VAR_METH), reqInfo, buff);

    } else if (svc.startsWith("rpc_ui_")) {
      uiBean.doService(svc, reqInfo, buff);

    } else {
      String msg = BeeUtils.concat(1, svc, "service type not recognized");
      LogUtils.warning(logger, msg);
      buff.addWarning(msg);
    }
  }
}
