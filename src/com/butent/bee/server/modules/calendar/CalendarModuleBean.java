package com.butent.bee.server.modules.calendar;

import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CalendarModuleBean implements BeeModule {

  private static Logger logger = Logger.getLogger(CalendarModuleBean.class.getName());

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;

  @Override
  public String dependsOn() {
    return CommonsConstants.COMMONS_MODULE;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CalendarConstants.CALENDAR_METHOD);

    if (BeeUtils.same(svc, CalendarConstants.SVC_GET_CONFIGURATION)) {
      response = getConfiguration();
    } else if (BeeUtils.same(svc, CalendarConstants.SVC_GET_USER_CALENDARS)) {
      response = getUserCalendars();
    } else {
      String msg = BeeUtils.concat(1, "Calendar service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public String getName() {
    return CalendarConstants.CALENDAR_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
  }

  private boolean checkTable(String name) {
    return sys.isTable(name) && sys.getTable(name).isActive();
  }

  private ResponseObject getConfiguration() {
    if (!checkTable(CalendarConstants.TBL_CONFIGURATION)) {
      return ResponseObject.error("table not active:", CalendarConstants.TBL_CONFIGURATION);
    }

    BeeRowSet res = sys.getViewData(CalendarConstants.VIEW_CONFIGURATION);
    return ResponseObject.response(res);
  }

  private ResponseObject getUserCalendars() {
    long userId = usr.getCurrentUserId();
    if (!checkTable(CalendarConstants.TBL_USER_CALENDARS)) {
      return ResponseObject.error("table not active:", CalendarConstants.TBL_USER_CALENDARS);
    }

    Filter filter = ComparisonFilter.isEqual(CalendarConstants.COL_USER, new LongValue(userId));
    BeeRowSet res = sys.getViewData(CalendarConstants.VIEW_USER_CALENDARS, filter, null);

    return ResponseObject.response(res);
  }
}
