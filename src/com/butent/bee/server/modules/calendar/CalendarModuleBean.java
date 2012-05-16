package com.butent.bee.server.modules.calendar;

import com.google.common.eventbus.Subscribe;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
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
  @EJB
  QueryServiceBean qs;

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
    } else if (BeeUtils.same(svc, CalendarConstants.SVC_GET_USER_CALENDAR)) {
      response = getUserCalendar(reqInfo);

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
    sys.registerViewEventHandler(new ViewEventHandler() {
      @SuppressWarnings("unused")
      @Subscribe
      public void initConfiguration(ViewQueryEvent event) {
        if (BeeUtils.same(event.getViewName(), CalendarConstants.VIEW_CONFIGURATION)) {
          BeeRowSet rowset = event.getRowset();

          if (rowset.isEmpty()) {
            rowset.addEmptyRow();
          }
        }
      }
    });
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

  private ResponseObject getUserCalendar(RequestInfo reqInfo) {
    if (!checkTable(CalendarConstants.TBL_USER_CALENDARS)) {
      return ResponseObject.error("table not active:", CalendarConstants.TBL_USER_CALENDARS);
    }

    long calendarId = BeeUtils.toLong(reqInfo.getParameter(CalendarConstants.PARAM_CALENDAR_ID));
    if (!DataUtils.isId(calendarId)) {
      return ResponseObject.error(CalendarConstants.SVC_GET_USER_CALENDAR,
          CalendarConstants.PARAM_CALENDAR_ID, "parameter not found");
    }

    long userId = usr.getCurrentUserId();

    Filter filter = Filter.and(
        ComparisonFilter.isEqual(CalendarConstants.COL_CALENDAR, new LongValue(calendarId)),
        ComparisonFilter.isEqual(CalendarConstants.COL_USER, new LongValue(userId)));

    BeeRowSet ucRowSet = sys.getViewData(CalendarConstants.VIEW_USER_CALENDARS, filter);
    if (!ucRowSet.isEmpty()) {
      return ResponseObject.response(ucRowSet);
    }

    BeeRowSet calRowSet = sys.getViewData(CalendarConstants.VIEW_CALENDARS,
        ComparisonFilter.compareId(calendarId));
    if (calRowSet.isEmpty()) {
      return ResponseObject.error(CalendarConstants.SVC_GET_USER_CALENDAR,
          CalendarConstants.PARAM_CALENDAR_ID, calendarId, "calendar not found");
    }

    CalendarSettings settings = CalendarSettings.create(calRowSet.getRow(0),
        calRowSet.getColumns());

    SqlInsert sqlInsert = new SqlInsert(CalendarConstants.TBL_USER_CALENDARS)
        .addConstant(CalendarConstants.COL_CALENDAR, calendarId)
        .addConstant(CalendarConstants.COL_USER, userId)
        .addConstant(CalendarConstants.COL_PIXELS_PER_INTERVAL, settings.getPixelsPerInterval())
        .addConstant(CalendarConstants.COL_INTERVALS_PER_HOUR, settings.getIntervalsPerHour())
        .addConstant(CalendarConstants.COL_WORKING_HOUR_START, settings.getWorkingHourStart())
        .addConstant(CalendarConstants.COL_WORKING_HOUR_END, settings.getWorkingHourEnd())
        .addConstant(CalendarConstants.COL_SCROLL_TO_HOUR, settings.getScrollToHour())
        .addConstant(CalendarConstants.COL_DEFAULT_DISPLAYED_DAYS,
            settings.getDefaultDisplayedDays())
        .addConstant(CalendarConstants.COL_ENABLE_DRAG_DROP, settings.isDragDropEnabled())
        .addConstant(CalendarConstants.COL_DRAG_DROP_CREATION,
            settings.isDragDropCreationEnabled())
        .addConstant(CalendarConstants.COL_OFFSET_HOUR_LABELS, settings.offsetHourLabels());

    if (settings.getTimeBlockClickNumber() != null) {
      sqlInsert.addConstant(CalendarConstants.COL_TIME_BLOCK_CLICK_NUMBER,
          settings.getTimeBlockClickNumber().ordinal());
    }

    ResponseObject response = qs.insertDataWithResponse(sqlInsert);
    if (response.hasErrors()) {
      return response;
    }

    BeeRowSet result = sys.getViewData(CalendarConstants.VIEW_USER_CALENDARS, filter);
    if (result.isEmpty()) {
      return ResponseObject.error(CalendarConstants.SVC_GET_USER_CALENDAR,
          CalendarConstants.PARAM_CALENDAR_ID, calendarId, "user calendar not created");
    }

    return ResponseObject.response(result);
  }
}
