package com.butent.bee.egg.server;

import com.butent.bee.egg.server.data.DataServiceBean;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.server.ui.UiLoaderBean;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;

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
  UiLoaderBean uiBean;
  @EJB
  MenuBean menuBean;

  public void doService(String svc, String dsn, RequestInfo reqInfo,
      ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

    if (BeeService.isDbService(svc)) {
      dataBean.doService(svc, dsn, reqInfo, buff);
    } else if (BeeService.isSysService(svc)) {
      sysBean.doService(svc, reqInfo, buff);
    } else if (BeeUtils.same(svc, BeeService.SERVICE_GET_MENU)) {
      menuBean.getMenu(buff);
    } else if (svc.startsWith("rpc_ui_")) {
      uiBean.doService(svc, reqInfo, buff);
    } else {
      String msg = BeeUtils.concat(1, svc, "service type not recognized");
      logger.warning(msg);
      buff.add(msg);
    }
  }

}
