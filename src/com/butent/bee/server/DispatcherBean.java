package com.butent.bee.server;

import com.google.common.collect.Maps;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.DataServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.ui.UiHolderBean;
import com.butent.bee.server.ui.UiServiceBean;
import com.butent.bee.server.utils.Reflection;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

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
  ModuleHolderBean moduleHolder;
  @EJB
  SystemServiceBean systemService;
  @EJB
  DataServiceBean dataService;
  @EJB
  UiServiceBean uiService;
  @EJB
  UiHolderBean uiHolder;
  @EJB
  Invocation invocation;
  @EJB
  UserServiceBean userService;
  @EJB
  SystemBean system;

  public ResponseObject doLogin(String locale, String host, String agent) {
    if (BeeUtils.isEmpty(SqlBuilderFactory.getDsn())) {
      return ResponseObject.error("DSN not specified");
    }
    
    ResponseObject response = new ResponseObject();
    Map<String, Object> data = Maps.newHashMap();
    
    ResponseObject userData = userService.login(locale, host, agent);
    response.addMessagesFrom(userData);
    if (userData.hasErrors()) {
      return response;
    }
    data.put(Service.LOGIN, userData.getResponse());
    
    ResponseObject menuData = uiHolder.getMenu();
    response.addMessagesFrom(menuData);
    if (menuData.hasErrors()) {
      return response;
    }
    data.put(Service.LOAD_MENU, menuData.getResponse());
    
    data.put(Service.GET_DATA_INFO, system.getDataInfo());
    
    BeeRowSet favorites = uiService.getFavorites();
    data.put(favorites.getViewName(), favorites);
    
    BeeRowSet filters = uiService.getFilters();
    data.put(filters.getViewName(), filters);
    
    ResponseObject decorators = uiService.getDecorators();
    response.addMessagesFrom(decorators);
    if (decorators.hasErrors()) {
      return response;
    }
    data.put(Service.GET_DECORATORS, decorators.getResponse());
    
    List<BeeRowSet> settings = uiService.getUserSettings();
    for (BeeRowSet rowSet : settings) {
      data.put(rowSet.getViewName(), rowSet);
    }
    
    response.setResponse(data);
    return response;
  }

  public void doLogout(String user) {
    userService.logout(user);
  }

  public ResponseObject doService(String svc, RequestInfo reqInfo, ResponseBuffer buff) {
    ResponseObject response = null;

    if (moduleHolder.hasModule(svc)) {
      response = moduleHolder.doModule(reqInfo);

    } else if (Service.isDataService(svc)) {
      response = uiService.doService(reqInfo);

    } else if (Service.isDbService(svc)) {
      dataService.doService(svc, reqInfo, buff);

    } else if (Service.isSysService(svc)) {
      response = systemService.doService(svc, reqInfo, buff);

    } else if (BeeUtils.same(svc, Service.LOAD_MENU)) {
      response = uiHolder.getMenu();

    } else if (BeeUtils.same(svc, Service.WHERE_AM_I)) {
      buff.addLine(buff.now(), BeeConst.whereAmI());

    } else if (BeeUtils.same(svc, Service.INVOKE)) {
      Reflection.invoke(invocation, reqInfo.getParameter(Service.RPC_VAR_METH), reqInfo, buff);

    } else {
      String msg = BeeUtils.joinWords(svc, "service not recognized");
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }

    return response;
  }
}
