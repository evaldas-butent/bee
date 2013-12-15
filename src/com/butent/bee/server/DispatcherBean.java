package com.butent.bee.server;

import com.google.common.collect.Maps;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.DataServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.ui.UiHolderBean;
import com.butent.bee.server.ui.UiServiceBean;
import com.butent.bee.server.utils.Reflection;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.ui.UserInterface.Component;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
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

  public ResponseObject doLogin(RequestInfo reqInfo) {
    ResponseObject response = new ResponseObject();
    Map<String, Object> data = Maps.newHashMap();

    ResponseObject userData = userService.login(reqInfo.getRemoteAddr(), reqInfo.getUserAgent());
    response.addMessagesFrom(userData);
    if (userData.hasErrors()) {
      return response;
    }
    data.put(Service.VAR_USER, userData.getResponse());

    UserInterface userInterface = null;

    String ui = reqInfo.getParameter(Service.VAR_UI);
    if (!BeeUtils.isEmpty(ui)) {
      userInterface = UserInterface.getByShortName(ui);
    }
    if (userInterface == null) {
      userInterface = userService.getUserInterface(reqInfo.getRemoteUser());
    }

    Collection<Component> components = UserInterface.normalize(userInterface).getComponents();

    if (!BeeUtils.isEmpty(components)) {
      for (Component component : components) {
        switch (component) {
          case AUTOCOMPLETE:
            ResponseObject acData = uiService.getAutocomplete();
            if (acData != null) {
              response.addMessagesFrom(acData);
              if (!acData.hasErrors() && acData.hasResponse()) {
                data.put(component.key(), acData.getResponse());
              }
            }
            break;
            
          case DATA_INFO:
            data.put(component.key(), system.getDataInfo());
            break;

          case DICTIONARY:
            data.put(component.key(),
                Localizations.getPreferredDictionary(userService.getLanguage()));
            break;

          case DECORATORS:
            ResponseObject decorators = uiService.getDecorators();
            if (decorators != null) {
              response.addMessagesFrom(decorators);
              if (!decorators.hasErrors() && decorators.hasResponse()) {
                data.put(component.key(), decorators.getResponse());
              }
            }
            break;

          case FAVORITES:
            BeeRowSet favorites = uiService.getFavorites();
            if (!DataUtils.isEmpty(favorites)) {
              data.put(component.key(), favorites);
            }
            break;

          case FILTERS:
            BeeRowSet filters = uiService.getFilters();
            if (!DataUtils.isEmpty(filters)) {
              data.put(component.key(), filters);
            }
            break;

          case GRIDS:
            Pair<BeeRowSet, BeeRowSet> settings = uiService.getGridAndColumnSettings();
            if (settings != null && !settings.isNull()) {
              data.put(component.key(), settings);
            }
            break;

          case MENU:
            ResponseObject menuData = uiHolder.getMenu();
            if (menuData != null) {
              response.addMessagesFrom(menuData);
              if (!menuData.hasErrors() && menuData.hasResponse()) {
                data.put(component.key(), menuData.getResponse());
              }
            }
            break;
        }
      }
    }

    return response.setResponse(data);
  }

  public void doLogout(long userId, long historyId) {
    userService.logout(userId, historyId);
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
