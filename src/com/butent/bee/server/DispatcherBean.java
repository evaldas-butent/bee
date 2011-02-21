package com.butent.bee.server;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.DataServiceBean;
import com.butent.bee.server.data.IdGeneratorBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.ui.UiServiceBean;
import com.butent.bee.server.utils.Reflection;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.sql.SqlBuilderFactory;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.logging.Level;
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
  UserServiceBean usrBean;
  @EJB
  IdGeneratorBean ig;
  @EJB
  SystemBean sys;

  public String doLogin(String dsn) {
    if (!BeeUtils.same(SqlBuilderFactory.getEngine(), BeeConst.getDsType(dsn))) {
      ig.destroy();
      sys.initDatabase(dsn);
    }
    String usr = usrBean.getUserSign();

    if (!BeeUtils.isEmpty(usr)) {
      LogUtils.infoNow(logger, "User logged in:", usr);
    }
    return usr;
  }

  public void doLogout(String usr) {
    LogUtils.infoNow(logger, "User logged out:", usr);
  }

  public void doService(String svc, String dsn, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

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

      if (svc.equals("rpc_ui_sql") && obj instanceof BeeRowSet) {
        buff.addColumns(((BeeRowSet) obj).getColumnArray());

        for (IsRow row : ((BeeRowSet) obj).getRows()) {
          for (int col = 0; col < ((BeeRowSet) obj).getNumberOfColumns(); col++) {
            buff.add(row.getString(col));
          }
        }
      } else if (obj instanceof BeeSerializable) {
        buff.add(((BeeSerializable) obj).serialize());
      } else {
        buff.add(obj);
      }
    } else {
      String msg = BeeUtils.concat(1, svc, "service type not recognized");
      LogUtils.warning(logger, msg);
      buff.addWarning(msg);
    }
  }
}
