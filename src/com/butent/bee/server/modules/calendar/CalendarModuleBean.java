package com.butent.bee.server.modules.calendar;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CalendarModuleBean implements BeeModule {

  private static Logger logger = Logger.getLogger(CalendarModuleBean.class.getName());

  @Override
  public String dependsOn() {
    return CommonsConstants.COMMONS_MODULE;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(CalendarConstants.CALENDAR_METHOD);

    if (BeeUtils.isPrefix(svc, "whatever")) {
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
}
