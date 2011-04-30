package com.butent.bee.server;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.DataServiceBean;
import com.butent.bee.server.data.IdGeneratorBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.ui.UiServiceBean;
import com.butent.bee.server.utils.Reflection;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Receives a service request and then passes it along, depending on service name structure, to an
 * according class for execution.
 */

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
  UserServiceBean usrBean;
  @EJB
  IdGeneratorBean ig;
  @EJB
  SystemBean sys;

  public String doLogin(String dsn, String locale) {
    if (!BeeUtils.same(SqlBuilderFactory.getEngine(), BeeConst.getDsType(dsn))) {
      ig.destroy();
      sys.initDatabase(dsn);
    }
    return usrBean.login(locale);
  }

  public void doLogout(String user) {
    usrBean.logout(user);
  }

  public void doService(String svc, String dsn, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

    if (Service.isDbService(svc)) {
      dataBean.doService(svc, dsn, reqInfo, buff);
    } else if (Service.isSysService(svc)) {
      sysBean.doService(svc, reqInfo, buff);

    } else if (BeeUtils.same(svc, Service.LOAD_MENU)) {
      menu.getMenu(reqInfo, buff);
    } else if (BeeUtils.same(svc, Service.WHERE_AM_I)) {
      buff.addLine(buff.now(), BeeConst.whereAmI());

    } else if (BeeUtils.same(svc, Service.INVOKE)) {
      Reflection.invoke(invBean, reqInfo.getParameter(Service.RPC_VAR_METH), reqInfo, buff);

    } else if (Service.isDataService(svc)) {
      ResponseObject resp = uiBean.doService(reqInfo);

      for (ResponseMessage msg : resp.getMessages()) {
        Level lvl = msg.getLevel();

        if (BeeUtils.equals(lvl, Level.SEVERE)) {
          buff.addSevere(msg);
        } else if (BeeUtils.equals(lvl, Level.WARNING)) {
          buff.addWarning(msg);
        } else {
          buff.addMessage(msg);
        }
      }
      Object obj = resp.getResponse();

      if (!BeeUtils.isEmpty(obj)) {
        buff.add(Codec.beeSerialize(obj));
      }
    } else {
      String msg = BeeUtils.concat(1, svc, "service type not recognized");
      LogUtils.warning(logger, msg);
      buff.addWarning(msg);
    }
  }
}
